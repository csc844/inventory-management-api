# 📦 Inventory Management API

> A production-ready RESTful API for managing products, suppliers, and real-time stock levels — built with Spring Boot 4 and native Hibernate SessionFactory.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?style=flat-square&logo=springboot)
![Hibernate](https://img.shields.io/badge/Hibernate-7.2-blue?style=flat-square&logo=hibernate)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## ✨ Features

- Full CRUD for Suppliers and Products
- Stock management (add, remove, track inventory)
- Real-time stock status (OK / LOW / REORDER)
- Global exception handling (consistent JSON responses)
- Bean validation on all DTOs
- Database indexing for performance
- SLF4J logging in service layer
- HikariCP connection pooling
- Native Hibernate SessionFactory (no Spring Data JPA)

---

## 🛠️ Tech Stack

| Layer | Technology |
|------|------------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| ORM | Hibernate 7.2 (SessionFactory) |
| Database | MySQL 8 |
| Connection Pool | HikariCP |
| Validation | Jakarta Bean Validation |
| Logging | SLF4J + Logback |
| Build Tool | Maven |
| Boilerplate | Lombok |

---

## 🏗️ Project Architecture


HTTP Request
↓
Controller
↓
Service (@Transactional)
↓
Repository (SessionFactory)
↓
MySQL Database


---

## 📂 Project Structure


src/main/java/com/inventory2/inventoryManagement2/

├── config/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── exception/
├── enums/
└── InventoryManagement2Application.java


---

## 🧠 Core Concepts

### SessionFactory
Native Hibernate SessionFactory used instead of JPA EntityManager.

### Transaction Management
Handled at service layer using:
```java
@Transactional
DTO Pattern

Entities are not exposed directly. DTOs used for request/response mapping.

Stock Status Logic
quantity <= minimumLevel          → REORDER
quantity <= minimumLevel * 2      → LOW
quantity > minimumLevel * 2       → OK
🗄️ Data Model
Supplier (1) ──── (N) Product (1) ──── (1) Stock
🚀 Getting Started
Prerequisites
Java 17+
MySQL 8
Database Setup
CREATE DATABASE inventory_db;
Run Project
./mvnw clean install
./mvnw spring-boot:run
📡 API Endpoints
Suppliers
Method	Endpoint
POST	/suppliers
GET	/suppliers
GET	/suppliers/{id}
PUT	/suppliers/{id}
DELETE	/suppliers/{id}
Products
Method	Endpoint
POST	/products
Stock
Method	Endpoint
POST	/stock/add
POST	/stock/remove
GET	/stock/{productId}
⚠️ Error Response Example
{
  "timestamp": "2026-06-10T10:00:00",
  "status": 404,
  "error": "Resource not found"
}
👨‍💻 Author

Ansh Singla

⭐ Support

If you like this project:

⭐ Star it
🍴 Fork it
🚀 Share it
<p align="center"> Made with ❤️ using Spring Boot & Hibernate </p> ```