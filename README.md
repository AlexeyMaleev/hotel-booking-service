# Hotel Booking Service

Backend service for managing hotel bookings.

Production-like backend service for managing hotel bookings.  
The project demonstrates backend development, integration testing,
and event-driven architecture using modern Java technologies.

---

## 📌 Project Overview

This service is designed to showcase:

- REST API design with Spring Boot
- Transactional business logic
- Role-based access control
- Integration with external infrastructure (PostgreSQL, Kafka, MongoDB)
- Automated integration testing with real containers

The project was developed as part of a professional Java backend training program
and further refined as a portfolio project.

---

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
- **PostgreSQL** — main relational database
- **Kafka** — event streaming
- **MongoDB** — analytical storage
- **Flyway** — database migrations
- **Docker & Docker Compose**
- **Gradle**
- **JUnit 5**
- **Testcontainers**
- **Swagger / OpenAPI**

---

## 🏗 Architecture Overview

- **REST API** for managing:
  - Hotels
  - Rooms
  - Bookings
  - Users
- **Relational data (PostgreSQL)** for core domain entities
- **Kafka** for publishing domain events
- **MongoDB** for analytical and statistical data
- **Security** implemented using role-based access control (`USER` / `ADMIN`)

---

## 🔐 Security

- Authentication and authorization implemented with **Spring Security**
- Role-based access control:
  - `USER` — can create and manage own bookings
  - `ADMIN` — can manage hotels and rooms
- Method-level security is used for fine-grained access control

---

## ⭐ Key Project Features

### 1. Reliable Booking System

The booking logic includes **overlap protection** to prevent double-booking
of the same room for intersecting date ranges.

This logic is implemented at the database level using optimized JPQL queries
in JPA repositories, ensuring high performance and data consistency even
under concurrent requests.

---

### 2. Asynchronous Analytics

When a new user is registered or a booking is created, corresponding domain
events are published to **Kafka**.

A dedicated consumer service processes these events asynchronously and persists
analytical data into **MongoDB**, allowing the system to collect statistics
without adding load to the main transactional database.

---

### 3. Reporting and Data Export

The application supports exporting accumulated statistics from MongoDB into
**CSV format**.

The download endpoint is implemented using asynchronous request processing,
enabling efficient handling of large datasets without blocking request threads.

---

### 4. Advanced Testing Strategy

The project uses a **Singleton Testcontainers pattern** for integration testing.

PostgreSQL, Kafka, and MongoDB containers are started in static initialization
blocks, which provides:

- Guaranteed infrastructure readiness before Spring context startup
- Faster build execution (containers are started once per test run)
- Stable and reproducible test results during `clean build`

---

### 5. Security (RBAC)

Role-based access control is implemented using Spring Security:

- **Registration:** Available for all users
- **Hotel and room management:** `ROLE_ADMIN`
- **Bookings:** `ROLE_USER`, `ROLE_ADMIN`
- **Statistics export:** Available only for `ROLE_ADMIN`

All endpoints are protected according to business requirements.

---

## 🐳 Infrastructure Configuration

The application is configured to work with containerized infrastructure defined in:

docker/docker-compose.yml

### PostgreSQL

- **Host:** localhost
- **Port:** 5438 (external port for application connection)
- **Database name:** `hotels_db`
- **Initialization:**  
  On first startup, the script `docker/init.sql` is executed to create the database schema

---

### Kafka

- **Bootstrap server:** `localhost:9092`
- **Topic:** `hotel-statistics-topic`

The topic is created automatically on the first published event.

Kafka is used for asynchronous delivery of domain events related to user registration
and booking creation.

---

### MongoDB

- **Connection URI:**
  mongodb://root:root@localhost:27017/bookingdatabase?authSource=admin
- **Database:** `bookingdatabase`

MongoDB is used to store analytical and statistical data consumed from Kafka events.

---

## 🚀 Project Startup

All commands are executed from the **root directory** of the project using a terminal
(**CMD** or **PowerShell**).

### 1. Pre-configuration

Create a file named `db-secret.yaml` in the project root directory.
This file is required to protect sensitive credentials and is already added
to `.gitignore`.

Example content:

```yaml
spring:
datasource:
  username: postgres
  password: password

2. Infrastructure Startup (Docker)

The project requires PostgreSQL, Kafka, and MongoDB.

To start all required services:

.\docker\docker-start.cmd

To stop and remove containers:

.\docker\docker-stop.cmd

3. Port Configuration

Default ports:

PostgreSQL: 5438

Kafka: 9092

MongoDB: 27017

If needed, update ports in:

docker-compose.yaml

corresponding section in application.yaml


4. Database Migrations

The application uses Flyway.

On first startup, database tables are created automatically from migration scripts located in:

src/main/resources/db/migration


5. Application Startup

Ensure db-secret.yaml exists in the project root.

Run the application:

gradlew bootRun


6. Running Tests

The project is covered with integration tests using Testcontainers.

Infrastructure containers are managed automatically.

Run tests:

gradlew clean test


📘 API Documentation

Swagger UI is available at:

http://localhost:8080/swagger-ui.html


⚙ Configuration

Sensitive configuration values (database credentials, secrets) are stored
outside the repository (see db-secret.yaml, excluded via .gitignore).


🎯 Project Goals

This project was created to demonstrate:

Clean backend architecture

Practical use of the Spring ecosystem

Working with relational and non-relational databases

Event-driven communication with Kafka

Writing reliable integration tests

Production-like development practices


👨‍💻 Author

Alexey Maleev
Java Backend Developer / AQA Engineer
markdown
[GitHub Profile](https://github.com/AlexeyMaleev)