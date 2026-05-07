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

- **Java 21 & Kotlin (active migration to Kotlin for test suites)**
- **Spring Boot 3**
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
- **JUnit 5 & Kotest**
  - MockMvc — for fluent API testing, security validation, and E2E flows.
  - AssertJ / JUnit 5 Assertions & Kotest Matchers — for clean and readable test checks.
- **Testcontainers** — for isolated infrastructure testing (Postgres, Kafka, MongoDB).
  - *Java:* traditional JUnit 5 integration.
  - *Kotlin:* advanced **Lazy Singleton pattern** for optimized resource usage.
- **Awaitility & Awaitility-Kotlin** — for testing eventual consistency in asynchronous event-driven flows.
- **Swagger / OpenAPI** — for API documentation and manual testing.
- **Project Reactor** — for reactive event processing (Kafka to MongoDB)

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
- **Testing Strategy:** Side-by-Side execution of JUnit 5 (Java) and Kotest (Kotlin)
to ensure smooth migration and maintainability.

---

### 🔄 CI/CD Optimization
- **Stable Infrastructure**: Sequential test execution (`maxParallelForks = 1`) is
configured to ensure stability in resource-constrained environments
(like GitHub Actions) when running multiple heavy Docker containers
(PostgreSQL, Kafka, MongoDB) simultaneously.

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

- **Guaranteed infrastructure readiness** before Spring context startup.
- **Faster build execution** — containers are started once per test run, covering both Integration and E2E suites.
- **Full-cycle E2E Validation** — tests simulate real user scenarios via MockMvc, including Spring Security checks, Kafka event streaming, and final state verification in MongoDB.
- **Reliable Async Assertions** — using Awaitility to handle eventual consistency when verifying data in MongoDB after Kafka processing.
- **Stable and reproducible results** during `clean build` in any environment.
- **Parallel Infrastructure Management:**
  - Java suites use standard Testcontainers integration for stability.
  - Kotlin suites leverage a **Lazy Singleton pattern** with `ApplicationContextInitializer` for dynamic port mapping and faster execution.

---

### 5. Security (RBAC)

Role-based access control is implemented using Spring Security:

- **Registration:** Available for all users
- **Hotel and room management:** `ROLE_ADMIN`
- **Bookings:** `ROLE_USER`, `ROLE_ADMIN`
- **Statistics export:** Available only for `ROLE_ADMIN`

All endpoints are protected according to business requirements.

---

### 6. End-to-End (E2E) Testing Flow
The project includes complex E2E tests that validate the entire business process across multiple infrastructure layers:
- **Trigger:** A REST API call initiates a booking or registration.
- **Process:** The system validates security (RBAC), executes transactional logic in **PostgreSQL**, and emits a **Kafka** event.
- **Async Validation:** Using **Awaitility**, the test waits for the asynchronous consumer to process the Kafka message and verifies the final state in **MongoDB**.

This ensures that all micro-integrations work together seamlessly in a production-like environment.

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
```  
  
### 2. Infrastructure Startup (Docker)

The project requires PostgreSQL, Kafka, and MongoDB.

To start all required services:

.\docker\docker-start.cmd

To stop and remove containers:

.\docker\docker-stop.cmd

### 3. Port Configuration

Default ports:

PostgreSQL: 5438

Kafka: 9092

MongoDB: 27017

If needed, update ports in:

docker-compose.yaml

corresponding section in application.yaml


### 4. Database Migrations

The application uses Flyway.

On first startup, database tables are created automatically from migration scripts located in:

src/main/resources/db/migration


### 5. Application Startup

Ensure db-secret.yaml exists in the project root.

Run the application:

gradlew bootRun


### 6. Running Tests

The project uses a hybrid engine (JUnit 5 + Kotest). Infrastructure is managed automatically via Testcontainers.
Run all tests (Java & Kotlin):

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