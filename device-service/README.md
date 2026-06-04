# Device Service

The **Device Service** is a core microservice in the Enerlytics ecosystem, responsible for managing smart devices, their
metadata, and their association with users. It provides a robust RESTful API to handle device lifecycle operations and
serves as a foundational component for energy usage tracking and alerting.

## 🚀 Core Features

- **Device Lifecycle Management**: Create, read, update, and delete smart devices.
- **User-Device Association**: Manage devices linked to specific user accounts.
- **Paginated Listings**: List endpoints return `Page<DeviceResponse>` with stable JSON (Spring Data `PagedModel`).
- **Optimistic Locking & Auditing**: `@Version` for safe concurrent updates; `createdAt`/`updatedAt` populated by Spring
  Data JPA auditing.
- **RFC 7807 Error Responses**: All errors are returned as `application/problem+json` via Spring's `ProblemDetail`.
- **Sealed Exception Hierarchy**: `ApplicationException` is `sealed` and `permits` a fixed set of concrete exceptions.
- **Virtual Threads**: All request handling and JDBC I/O run on virtual threads.
- **Kubernetes-Ready**: Liveness and readiness probes exposed via Actuator.
- **Data Persistence**: PostgreSQL with Flyway-managed versioned schema migrations.
- **API Documentation**: Interactive Swagger/OpenAPI UI via Springdoc.
- **Health & Monitoring**: Integrated with Spring Boot Actuator and Prometheus.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0.6
- **Database**: PostgreSQL
- **Migration**: Flyway
- **Build Tool**: Maven
- **Test Stack**: JUnit 5, Mockito, AssertJ, Testcontainers
- **Documentation**: Springdoc OpenAPI
- **Quality**: Spotless (Palantir Java Format), JSpecify nullability annotations

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

The service will be available at `http://localhost:8081` (or the port specified by `SERVER_PORT`).

## 🧪 Testing

The project uses a split between **Surefire** (unit tests) and **Failsafe** (integration tests):

| Plugin   | Pattern      | Description                                             |
|:---------|:-------------|:--------------------------------------------------------|
| Surefire | `*Test.java` | Unit tests — JUnit 5 + Mockito + AssertJ, no Spring ctx |
| Failsafe | `*IT.java`   | Integration tests — `@SpringBootTest` + Testcontainers  |

### Run All Tests

```bash
./mvnw verify
```

### Run Only Unit Tests

```bash
./mvnw test
```

### Run Only Integration Tests

```bash
./mvnw failsafe:integration-test failsafe:verify
```

### Integration Tests with Testcontainers

Integration tests (`*IT.java`) use **Testcontainers** to spin up a real PostgreSQL container.

- **Container Image**: `postgres:18-alpine`
- **Autoconfiguration**: Spring Boot's `@ServiceConnection` wires the datasource automatically — no manual config.
- **Lifecycle**: Container starts before the test class and stops after.
- **Reset**: Each test class cleans its own data via `@BeforeEach deviceRepository.deleteAll()`.

## 📖 API Documentation

Once the service is running, the interactive API documentation is available at:

- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8081/v3/api-docs`

### Error Responses

Errors are returned as `application/problem+json` per [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807):

```json
{
  "type": "/errors/not-found",
  "title": "ResourceNotFoundException",
  "status": 404,
  "detail": "Device not found with id: 42",
  "instance": "/api/v1/devices/42",
  "timestamp": "2026-06-04T20:19:02Z"
}
```

The `type` URI identifies the kind of problem (e.g., `/errors/not-found`, `/errors/conflict`, `/errors/validation`).

## ⚙️ Configuration

### Profiles

| Profile   | Purpose                          | Notes                                                                               |
|:----------|:---------------------------------|:------------------------------------------------------------------------------------|
| (default) | Base config — works for dev/test | Datasource defaults to `localhost:5432/devices`                                     |
| `prod`    | Production tuning                | Larger Hikari pool, `INFO` log level, ECS structured logging, `show-details: never` |

Activate a profile with `--spring.profiles.active=prod` or the `SPRING_PROFILES_ACTIVE` environment variable.

### Environment Variables

| Variable                     | Description                               | Default                                    |
|:-----------------------------|:------------------------------------------|:-------------------------------------------|
| `SERVER_PORT`                | HTTP server port                          | `8081`                                     |
| `DB_URL`                     | PostgreSQL JDBC URL                       | `jdbc:postgresql://localhost:5432/devices` |
| `DB_USERNAME`                | PostgreSQL username                       | `postgres`                                 |
| `DB_PASSWORD`                | PostgreSQL password                       | `password`                                 |
| `OPENAPI_SERVER_URL`         | OpenAPI server URL                        | `http://localhost:8081`                    |
| `OPENAPI_SERVER_DESCRIPTION` | OpenAPI server description                | `Local environment`                        |
| `INIT_DATA`                  | Seed sample devices on startup (dev only) | `false`                                    |

### Actuator Endpoints

| Endpoint                     | Purpose                                             |
|:-----------------------------|:----------------------------------------------------|
| `/actuator/health`           | Liveness/readiness summary (details hidden in prod) |
| `/actuator/health/liveness`  | K8s liveness probe                                  |
| `/actuator/health/readiness` | K8s readiness probe                                 |
| `/actuator/prometheus`       | Prometheus metrics                                  |
| `/actuator/metrics`          | Micrometer metrics                                  |

## 🛠 Development

### Formatting

Spotless enforces a consistent Palantir Java Format across the codebase.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`

### Database Migrations

Flyway manages migrations. SQL scripts are located at `src/main/resources/db/migration`.

| Version | Description                                                               |
|:--------|:--------------------------------------------------------------------------|
| `V1`    | Initial `devices` table                                                   |
| `V2`    | Audit columns (`version`, `created_at`, `updated_at`) and `user_id` index |

New migrations must follow the naming convention `V<Version>__<Description>.sql`. All `Instant` columns use
`TIMESTAMP WITH TIME ZONE` (`TIMESTAMPTZ`) to preserve timezone semantics for `Instant` round-trips.

### Exception Hierarchy

All application-thrown exceptions extend the sealed `ApplicationException` (in `com.enerlytics.devices.exceptions`):

```
ApplicationException (sealed)
├── ResourceNotFoundException   → 404 Not Found
├── DuplicateResourceException  → 409 Conflict
├── BusinessRuleException       → 422 Unprocessable Content
└── ExternalServiceException    → 502 Bad Gateway
```

To add a new application exception, extend `ApplicationException` and add it to the `permits` clause.
