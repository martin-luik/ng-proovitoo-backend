# Events App – Backend

A backend service for managing events, built with Java and Spring Boot.

---

## 🚀 Tech Stack

- **Java 21 (LTS)**
- **Spring Boot 3.5.5**
- **Gradle** (build tool)
- **PostgreSQL** + **Spring Data JPA**
- **JWT Security** (admin endpoints)
- **OpenAPI / Swagger** (`springdoc-openapi`)
- **Testing:** JUnit 5, Testcontainers (Postgres)

---

## 🏃‍♂️ Running Locally

1. **Start the application:**
    ```bash
    ./gradlew bootRun
    ```

2. **API Endpoints:**
    - [Events API](http://localhost:8080/v1/events)
    - [Swagger UI](http://localhost:8080/swagger-ui/index.html)
    - [OpenAPI JSON](http://localhost:8080/api-docs)
    - [Health Check](http://localhost:8080/actuator/health)

---

## 🔐 Authentication

- **Default Admin**
    - **Email:** `admin@example.com`
    - **Password:** Configurable (bcrypt hash required)

  Generate a bcrypt hash:
  ```bash
  ./gradlew hashPassword -Ppwd=admin123
  ```

- **Login:**
    - `POST /auth/login` → returns JWT token

- **Admin Endpoints:**
    - Require `Authorization: Bearer <token>` header

---

## 🐳 Docker

Run the app and Postgres together:
```bash
docker compose up -d --build
```

---

## 🧪 Testing

- Unit and integration tests with **JUnit 5**
- Repository tests use **Testcontainers** (Postgres 16)
- If Docker is unavailable, integration tests are skipped

Run tests:
```bash
./gradlew test
```

---

## 📝 Project Notes

- DTO validation via `@Valid` (Bean Validation)
- Entities mapped with **MapStruct**
- Errors returned as JSON with proper status codes
- Clear package structure:
    - `event/`
    - `registration/`
    - `auth/`
    - `config/`