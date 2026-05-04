# Device Service

The **Device Service** is a core microservice in the Enerlytics ecosystem, responsible for managing smart devices, their
metadata, and their association with users. It provides a robust RESTful API to handle device lifecycle operations and
serves as a foundational component for energy usage tracking and alerting.

## 🚀 Core Features

- **Device Lifecycle Management**: Create, read, update, and delete smart devices.
- **User-Device Association**: Manage devices linked to specific user accounts.
- **Device Metadata**: Store device types, locations, and other identifying information.
- **Data Persistence**: Managed with PostgreSQL and Flyway for versioned schema migrations.
- **API Documentation**: Interactive Swagger/OpenAPI UI.
- **Health & Monitoring**: Integrated with Spring Boot Actuator and Prometheus.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0
- **Database**: PostgreSQL
- **Migration**: Flyway
- **Build Tool**: Maven
- **Documentation**: Springdoc OpenAPI
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for running PostgreSQL via `compose.yaml` at the root)

### Running the Service

1. **Start Infrastructure**: From the project root, start the database.
   ```bash
   docker compose -f compose.yaml up -d postgres
   ```

2. **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
   ```bash
   cp .env.example .env
   ```

3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The service will be available at `http://localhost:8081` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

### Integration Tests with Testcontainers

Integration tests use **Testcontainers** to spin up a real PostgreSQL database in a Docker container.
The database is automatically managed and cleaned up between test runs.

- **Container Image**: `postgres:18-alpine`
- **Database**: Isolated per test class (e.g., `devices`)
- **Lifecycle**: Container starts before all tests and stops after completion
- **Autoconfiguration**: Spring Boot's `@ServiceConnection` automatically configures the datasource

No manual database setup is required for running tests — Testcontainers handles everything.

## 📖 API Documentation

Once the service is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8081/v3/api-docs`

## ⚙️ Configuration

Key environment variables:

| Variable      | Description                       | Default     |
|:--------------|:----------------------------------|:------------|
| `SERVER_PORT` | Port the service runs on          | `8081`      |
| `DB_HOST`     | PostgreSQL host                   | `localhost` |
| `DB_PORT`     | PostgreSQL port                   | `5432`      |
| `DB_NAME`     | Database name                     | `devices`   |
| `DB_USERNAME` | Database username                 | `postgres`  |
| `DB_PASSWORD` | Database password                 | `password`  |
| `INIT_DATA`   | Whether to initialize sample data | `false`     |

## 🛠 Development

### Formatting

This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`

### Database Migrations

Migrations are handled by Flyway. SQL scripts are located in:
`src/main/resources/db/migration`

New migrations should follow the naming convention: `V<Version>__<Description>.sql`.
