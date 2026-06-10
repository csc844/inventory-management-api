# 📦 Inventory Management API

> A production-ready RESTful API for managing products, suppliers, and real-time stock levels — built with Spring Boot 4 and native Hibernate SessionFactory.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?style=flat-square&logo=springboot)
![Hibernate](https://img.shields.io/badge/Hibernate-7.2-blue?style=flat-square&logo=hibernate)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## ✨ Features

- ✅ Full *CRUD* for Suppliers and Products
- ✅ *Stock management* — add, remove, and track inventory levels
- ✅ *Real-time stock status* — OK / LOW / REORDER computed on every read
- ✅ *Global exception handling* with consistent JSON error responses
- ✅ *Bean Validation* on all request DTOs
- ✅ *Database indexing* on frequently queried columns
- ✅ *SLF4J logging* across all service operations
- ✅ *HikariCP* connection pooling
- ✅ Native *Hibernate SessionFactory* (no Spring Data JPA abstraction)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 (Spring Framework 7) |
| ORM | Hibernate 7.2 (native SessionFactory) |
| Database | MySQL 8 |
| Connection Pool | HikariCP 7 |
| Validation | Jakarta Bean Validation |
| Logging | SLF4J + Logback (via Lombok @Slf4j) |
| Build Tool | Maven |
| Boilerplate Reduction | Lombok |

---

## 🏗️ Project Architecture

This project follows a *layered architecture* with a clean separation of concerns:


HTTP Request
│
▼
┌─────────────┐
│  Controller │  ← Receives HTTP, delegates to service
└──────┬──────┘
│
▼
┌─────────────┐
│   Service   │  ← Business logic, @Transactional boundary, SLF4J logging
└──────┬──────┘
│
▼
┌─────────────┐
│ Repository  │  ← Data access via Hibernate SessionFactory
└──────┬──────┘
│
▼
┌─────────────┐
│   MySQL DB  │  ← Indexed tables: suppliers, products, stocks
└─────────────┘


*Key architectural decisions:*
- Repositories use native Hibernate SessionFactory (getCurrentSession()) instead of Spring Data JPA
- Transaction boundaries are owned by the *service layer* via @Transactional
- HibernateConfig builds SessionFactory directly from DataSource using LocalSessionFactoryBean, avoiding any circular dependency with JPA auto-configuration
- JPA auto-configuration is excluded since only native Hibernate is used

---

## 📂 Project Structure


src/main/java/com/inventory2/inventoryManagement2/
│
├── config/
│   └── HibernateConfig.java          # SessionFactory, DataSource, TransactionManager beans
│
├── controller/
│   ├── ProductController.java
│   ├── StockController.java
│   └── SupplierController.java
│
├── service/                           # @Transactional + @Slf4j logging
│   ├── ProductService.java
│   ├── StockService.java
│   └── SupplierService.java
│
├── repository/                        # Native Hibernate (SessionFactory, not JpaRepository)
│   ├── ProductRepository.java
│   ├── StockRepository.java
│   └── SupplierRepository.java
│
├── entity/                            # @Index annotations for DB performance
│   ├── Product.java                   # ManyToOne → Supplier
│   ├── Stock.java                     # OneToOne → Product
│   └── Supplier.java                  # OneToMany → Products
│
├── dto/
│   ├── ProductRequestDto.java / ProductResponseDto.java
│   ├── StockRequestDto.java  / StockResponseDto.java
│   └── SupplierRequestDto.java / SupplierResponseDto.java
│
├── exception/
│   ├── GlobalExceptionHandler.java    # @RestControllerAdvice
│   ├── ResourceNotFoundException.java # 404 responses
│   └── InsufficientStockException.java# 400 responses
│
├── enums/
│   └── ProductStatus.java             # ACTIVE, INACTIVE, OUT_OF_STOCK
│
└── InventoryManagement2Application.java


---

## 🧠 Concept Implementation

### Native Hibernate SessionFactory
Repositories inject SessionFactory via constructor and call getCurrentSession(), which requires an active Spring-managed transaction. HibernateConfig builds the factory directly from DataSource using LocalSessionFactoryBean — no dependency on EntityManagerFactory, no circular dependency.

### Transaction Management
@Transactional is placed at the *service layer*. Each service method opens a transaction; the bound Hibernate session is shared across all repository calls within that method and committed or rolled back as a single unit.

### DTO Pattern
Entities are never exposed directly. Controllers accept *RequestDto objects (validated with Bean Validation), services map them to entities and back to *ResponseDto objects manually — decoupling the API contract from the database schema.

### Stock Status Logic
Stock status is *not persisted* — derived at read time in StockService.mapToDto():


quantity ≤ minimumLevel          →  REORDER
quantity ≤ minimumLevel × 2      →  LOW
quantity > minimumLevel × 2      →  OK


Default minimumLevel is 10.

### Database Indexing
Indexes defined via @Index in @Table annotations — created automatically by Hibernate on startup:

| Table | Index | Column | Type |
|---|---|---|---|
| suppliers | idx_supplier_email | email | Unique |
| suppliers | idx_supplier_name | name | Regular |
| products | idx_product_supplier | supplier_id | Regular |
| products | idx_product_status | status | Regular |
| products | idx_product_name | name | Regular |
| stocks | idx_stock_product | product_id | Unique |

### SLF4J Logging
All service classes use Lombok's @Slf4j annotation. Log levels:
- INFO — every operation start and successful completion
- WARN — resource not found, insufficient stock
- DEBUG — internal details (quantities, stock status) hidden in production

### Global Exception Handling
@RestControllerAdvice intercepts all exceptions and returns a uniform JSON error envelope:

| Exception | HTTP Status |
|---|---|
| ResourceNotFoundException | 404 Not Found |
| InsufficientStockException | 400 Bad Request |
| MethodArgumentNotValidException | 400 Bad Request (with field errors) |
| Exception (catch-all) | 500 Internal Server Error |

---

## 🗄️ Data Model


Supplier (1) ──────── (N) Product (1) ──────── (1) Stock
- id                      - id                     - id
- name                    - name                   - quantity
- email (unique)          - price                  - minimumLevel
- phone                   - status (enum)          - createdAt
- createdAt               - createdAt


---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8 running on localhost:3306

### Database Setup

sql
CREATE DATABASE inventory_db;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;


### Run

bash
./mvnw spring-boot:run


API base URL: http://localhost:8080

### Build & Test

bash
# Build
./mvnw clean install

# Run all tests
./mvnw test

# Run a single test
./mvnw test -Dtest=InventoryManagement2ApplicationTests


---

## 📡 API Endpoints

### 🏭 Suppliers

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /suppliers | Create a new supplier |
| GET | /suppliers | Get all suppliers |
| GET | /suppliers/{id} | Get supplier by ID |
| PUT | /suppliers/{id} | Update supplier |
| DELETE | /suppliers/{id} | Delete supplier |

*Request Body:*
json
{
"name": "ABC Supplies",
"email": "abc@supplies.com",
"phone": "9876543210"
}


---

### 📦 Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /products | Create a new product |

*Request Body:*
json
{
"name": "Laptop",
"price": 75000.00,
"supplierId": 1
}


---

### 📊 Stock

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /stock/add?productId=1&quantity=50 | Add stock |
| POST | /stock/remove?productId=1&quantity=10 | Remove stock |
| GET | /stock/{productId} | Get stock details and status |

*Stock Response:*
json
{
"id": 1,
"productName": "Laptop",
"quantity": 25,
"minimumLevel": 10,
"status": "LOW",
"createdAt": "2026-06-10T10:00:00"
}


---

## ⚠️ Error Responses

json
{
"timestamp": "2026-06-10T10:00:00",
"status": 404,
"error": "Supplier not found with id: 5"
}


*Validation errors:*
json
{
"timestamp": "2026-06-10T10:00:00",
"status": 400,
"errors": {
"name": "Product name is required",
"price": "Price must be positive"
}
}


---

## ⚙️ Configuration

src/main/resources/application.properties

properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true


---

## 👨‍💻 Author

*Ansh Singla*

> Built with passion for clean architecture and hands-on Hibernate mastery.

---

<p align="center">Made with ❤️ using Spring Boot & Hibernate</p>