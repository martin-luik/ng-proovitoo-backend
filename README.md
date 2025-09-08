# Events App – Backend

A backend service for managing events, built with Java and Spring Boot.

---

## 🚀 Tech Stack

- **Java 21**
- **Spring Boot 3.5.5**
- **Gradle** (build tool)
- **PostgreSQL 16** + **Spring Data JPA**
- **Liquibase** (database migrations)
- **MapStruct** (DTO ↔ Entity mapping)
- **JWT Security** (HttpOnly cookie, admin endpoints via OAuth2 Resource Server)
- **OpenAPI / Swagger** (`springdoc-openapi` 2.x)
- **Spring Boot Actuator** + **Micrometer** (monitoring & health checks)
- **Logging:** Logback (JSON/console config)
- **Testing:** JUnit 5, Testcontainers (Postgres, requires Docker)

---

## 🏃‍♂️ Running Locally

1. **The backend requires a **PostgreSQL** database.  
   You can start it with the included Docker Compose file:**
    ```bash
    docker compose up -d
   ```

2. **Build the project:**
    ```bash
    ./gradlew build
    ```

3. **Start the application:**
    ```bash
    ./gradlew bootRun
    ```

4. **API Endpoints:**
    - [Events API](http://localhost:8080/v1/events)
    - [Swagger UI](http://localhost:8080/swagger-ui/index.html)
    - [OpenAPI JSON](http://localhost:8080/api-docs)
    - [Health Check](http://localhost:8080/actuator/health)

> **Optional:** If you use an IDE like IntelliJ IDEA or VS Code, you can simply open the project folder.  
> IntelliJ automatically imports Gradle projects. In VS Code, install the Java and Gradle extensions if needed.

---

## 🔐 Authentication

### Default Admin

- **Email:** `admin@example.com`
- **Password:** `admin123`

The password must be stored as a **bcrypt hash** in `application.yml` under the `admin.passwordHash` property.


To generate a bcrypt hash for your password:
```bash
./gradlew hashPassword -Ppwd=admin123
```

---

### Login Flow

- **Login:**  
  `POST /auth/login`  
  On success, sets an **HttpOnly cookie** with a JWT.
- **Admin Endpoints:**  
  Require the authentication cookie (`access_token`), which is sent automatically by the browser.

---

### Security Notes

The implementation uses **JWT tokens stored in an HttpOnly cookie**.  
This prevents JavaScript access to the token, mitigating XSS risks.

#### Cookie Attributes

- `HttpOnly=true`
- `SameSite=Lax` (default; use `Strict` for tighter security)
- `Secure=false` in local development, `Secure=true` in production (HTTPS required)
- `Max-Age` matches JWT expiry
- **No tokens** are stored in `localStorage` or `sessionStorage`
- **Logout:** Implemented by expiring the cookie (`Max-Age=0`)

#### Development & Deployment

- In development, the Angular proxy forwards `/auth` and `/v1` requests with credentials enabled.
---

### Production Hardening Checklist

- Always run behind **TLS (HTTPS)** and set `Secure=true` on cookies.
- Configure **CORS** only if frontend and backend are on different origins.
- Optionally add **CSRF protection** (e.g., double-submit cookie pattern).
---

## 🐳 Docker

### Build Backend Image Manually

**If you want to build the backend image separately (without `docker compose`), run:**

```bash
docker build -t events/backend:1.0.0 .
```

**You can then run it with:**
```bash
docker run --rm -p 8080:8080 events/backend:1.0.0
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
## 📦 Database Migrations

The project uses **Liquibase** to manage database schema changes.

- Changesets are defined under `src/main/resources/db.changelog/`.
- For the initial schema, a SQL-based changeset is used (`changelog_V2025_09_02_21_09_init.sql`).
- The master file (`db.changelog-master.xml`) includes all changesets.

👉 In production, schema updates would be added as new incremental changesets (instead of editing the initial one).  
Rollback scripts could also be added if needed.
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
