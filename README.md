# Inventory Management API

> A RESTful API for managing products, suppliers, and real-time stock levels — built with Spring Boot 4 and native Hibernate SessionFactory, with Kafka event streaming, multi-tier caching, and persistent stock history.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?style=flat-square&logo=springboot)
![Hibernate](https://img.shields.io/badge/Hibernate-7.2-blue?style=flat-square&logo=hibernate)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7.x-red?style=flat-square&logo=redis)

---

## Features

- Full **CRUD** for Suppliers and Products
- **Stock management** — add, remove, and track inventory levels
- **Real-time stock status** — `OK` / `LOW` / `REORDER` computed on every read
- **Kafka producer + consumer** — stock change events published and consumed within the same service
- **Stock history table** — every stock mutation is persisted from the Kafka consumer
- **Multi-tier caching** — Redis, LRU, and Caffeine caches across different endpoints
- **Global exception handling** with consistent JSON error responses
- **Bean Validation** on all request DTOs
- **Database indexing** on frequently queried columns
- **HikariCP** connection pooling
- Native **Hibernate SessionFactory** (no Spring Data JPA abstraction)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| ORM | Hibernate 7.2 (native SessionFactory) |
| Database | MySQL 8 |
| Message Broker | Apache Kafka |
| Cache — Distributed | Redis 7 |
| Cache — In-Memory LRU | Java `LinkedHashMap` (access-order) |
| Cache — In-Memory TTL | Caffeine |
| Connection Pool | HikariCP |
| Validation | Jakarta Bean Validation |
| Logging | SLF4J + Logback (`@Slf4j`) |
| Build Tool | Maven |
| Boilerplate | Lombok |

---

## Architecture

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller │  ← Receives HTTP, delegates to service
└──────┬──────┘
       │
       ▼
┌────────────────────────────────────┐
│            Service                 │
│  ┌──────────┐  ┌────────────────┐  │
│  │  Cache   │  │ Kafka Producer │  │  ← Check cache first; publish event on mutation
│  │(Redis /  │  └────────┬───────┘  │
│  │LRU /     │           │          │
│  │Caffeine) │           ▼          │
│  └──────────┘    Kafka Topic       │
└──────┬─────────────────────────────┘
       │                  │
       ▼                  ▼
┌─────────────┐   ┌───────────────────┐
│ Repository  │   │  KafkaConsumer    │  ← Persists to stock_history table
│ (Hibernate) │   └───────────────────┘
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   MySQL DB  │
└─────────────┘
```

---

## Project Structure

```
src/main/java/com/inventory2/inventoryManagement2/
│
├── config/
│   ├── HibernateConfig.java        # SessionFactory, DataSource, TransactionManager
│   ├── KafkaConfig.java            # ProducerFactory, ConsumerFactory, KafkaTemplate
│   └── CaffeineConfig.java         # Caffeine Cache<String, StockResponseDto> bean
│
├── kafka/
│   ├── StockEvent.java             # Event DTO: productId, operation, quantity, timestamp
│   ├── KafkaProducerService.java   # Publishes StockEvent to "stock-events" topic
│   └── KafkaConsumerService.java   # Consumes events, saves to stock_history table
│
├── cache/
│   └── LruCacheService.java        # Thread-safe LRU via LinkedHashMap (access-order)
│
├── controller/
│   ├── ProductController.java
│   ├── StockController.java
│   └── SupplierController.java
│
├── service/
│   ├── ProductService.java         # Redis write-through on create
│   ├── StockService.java           # Redis write-through + Caffeine read cache + Kafka publish
│   └── SupplierService.java        # Redis write-through + LRU read cache
│
├── repository/
│   ├── ProductRepository.java
│   ├── StockRepository.java
│   ├── StockHistoryRepository.java # Stores history records consumed from Kafka
│   └── SupplierRepository.java
│
├── entity/
│   ├── Product.java                # ManyToOne → Supplier
│   ├── Stock.java                  # OneToOne → Product
│   ├── StockHistory.java           # History record: operation, quantityChanged, timestamp
│   └── Supplier.java               # OneToMany → Products
│
├── dto/
│   ├── ProductRequestDto.java / ProductResponseDto.java
│   ├── StockRequestDto.java  / StockResponseDto.java
│   └── SupplierRequestDto.java / SupplierResponseDto.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── InsufficientStockException.java
│
├── enums/
│   └── ProductStatus.java          # ACTIVE, INACTIVE, OUT_OF_STOCK
│
└── InventoryManagement2Application.java
```

---

## Kafka — Producer & Consumer

Both the producer and consumer live in the same service.

**Topic:** `stock-events`

**Producer** (`KafkaProducerService`) — called by `StockService` after every `addStock` or `removeStock`:
```json
{
  "productId": 1,
  "productName": "Laptop",
  "operation": "ADD",
  "quantityChanged": 50,
  "quantityAfter": 75,
  "timestamp": "2026-06-12T10:30:00"
}
```

**Consumer** (`KafkaConsumerService`) — listens on the same topic and persists each event to the `stock_history` table:

```
stock_history
─────────────────────────────────────────────────
id | productId | productName | operation | quantityChanged | quantityAfter | timestamp
```

Kafka is configured with a custom `ObjectMapper` (JavaTimeModule) so `LocalDateTime` serializes as ISO-8601 strings.

---

## Caching Strategy

### Fetch endpoints

| Endpoint | Cache | Behaviour |
|---|---|---|
| `GET /suppliers/{id}` | **Redis** (30 min TTL) | Redis hit → return; miss → DB → put in Redis |
| `GET /suppliers` | **LRU** (max 100 entries) | LRU hit → return; miss → DB → put in LRU |
| `GET /stock/{productId}` | **Caffeine** (10 min TTL, max 500) | Caffeine hit → return; miss → DB → put in Caffeine |

All cache reads fall back to the DAO on miss or error — cache failures never break the API.

### Save / update endpoints

| Endpoint | Action |
|---|---|
| `POST /suppliers` | Save to Redis; evict LRU |
| `PUT /suppliers/{id}` | Update Redis; evict LRU |
| `DELETE /suppliers/{id}` | Evict from Redis; evict LRU |
| `POST /stock/add` | Save to Redis + Caffeine |
| `POST /stock/remove` | Save to Redis + Caffeine |
| `POST /products` | Save to Redis |

---

## Data Model

```
Supplier (1) ──────── (N) Product (1) ──────── (1) Stock
  - id                      - id                     - id
  - name                    - name                   - quantity
  - email (unique)          - price                  - minimumLevel
  - phone                   - status (enum)          - createdAt
  - createdAt               - createdAt

StockHistory
  - id
  - productId
  - productName
  - operation          (ADD / REMOVE)
  - quantityChanged
  - quantityAfter
  - timestamp
```

### Stock status logic (computed at read time, not persisted)

```
quantity ≤ minimumLevel        →  REORDER
quantity ≤ minimumLevel × 2    →  LOW
quantity > minimumLevel × 2    →  OK
```

Default `minimumLevel` = `10`.

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8 on `localhost:3306`
- Apache Kafka on `localhost:9092`
- Redis on `localhost:6379`

### Database Setup

```sql
CREATE DATABASE inventory_db;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
```

Hibernate DDL is set to `update` — all tables (`suppliers`, `products`, `stocks`, `stock_history`) are created automatically on startup.

### Start Kafka (local)

```bash
# Start ZooKeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka broker
bin/kafka-server-start.sh config/server.properties
```

### Run the application

```bash
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Build & Test

```bash
./mvnw clean install
./mvnw test
```

---

## API Endpoints

### Suppliers

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| `POST` | `/suppliers` | Create supplier | Writes to Redis |
| `GET` | `/suppliers` | List all suppliers | LRU cache |
| `GET` | `/suppliers/{id}` | Get supplier by ID | Redis cache |
| `PUT` | `/suppliers/{id}` | Update supplier | Writes to Redis |
| `DELETE` | `/suppliers/{id}` | Delete supplier | Evicts Redis + LRU |

**Request body:**
```json
{
  "name": "ABC Supplies",
  "email": "abc@supplies.com",
  "phone": "9876543210"
}
```

---

### Products

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| `POST` | `/products` | Create product | Writes to Redis |

**Request body:**
```json
{
  "name": "Laptop",
  "price": 75000.00,
  "supplierId": 1
}
```

---

### Stock

| Method | Endpoint | Description | Cache |
|--------|----------|-------------|-------|
| `POST` | `/stock/add?productId=1&quantity=50` | Add stock | Writes to Redis + Caffeine; publishes Kafka event |
| `POST` | `/stock/remove?productId=1&quantity=10` | Remove stock | Writes to Redis + Caffeine; publishes Kafka event |
| `GET` | `/stock/{productId}` | Get stock | Caffeine cache |

**Stock response:**
```json
{
  "id": 1,
  "productName": "Laptop",
  "quantity": 25,
  "minimumLevel": 10,
  "status": "LOW",
  "createdAt": "2026-06-12T10:00:00"
}
```

---

## Configuration

`src/main/resources/application.properties`

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## Key Concepts

### Native Hibernate SessionFactory
Repositories inject `SessionFactory` directly and call `getCurrentSession()` per operation. `HibernateConfig` builds the factory from `DataSource` via `LocalSessionFactoryBean` — no JPA abstraction, no `EntityManagerFactory`.

### Transaction Management
`@Transactional` is at the service layer. Each service method owns a single transaction shared across all repository calls within it. The Kafka consumer method is also `@Transactional` so history saves are atomic.

### DTO Pattern
Entities are never exposed directly. Controllers accept `*RequestDto` (Bean Validation), services map to entities and return `*ResponseDto` — decoupling API contract from DB schema.

### Cache Failure Safety
All Redis reads/writes are wrapped in try-catch — a Redis outage degrades gracefully to DB-only operation without throwing to the caller.