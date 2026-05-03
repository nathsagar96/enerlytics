# User Service

The **User Service** is a core microservice in the Enerlytics ecosystem, responsible for managing user profiles, preferences, and alerting configurations. It provides a robust RESTful API to handle user-related operations and integrates with the overall monitoring and alerting flow.

## 🚀 Core Features

- **User Profile Management**: Create, read, update, and delete user profiles.
- **Alerting Preferences**: Configure user-specific energy threshold alerts.
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

1.  **Start Infrastructure**: From the project root, start the database.
    ```bash
    docker compose -f compose.yaml up -d users-db
    ```

2.  **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
    ```bash
    cp .env.example .env
    ```

3.  **Run Application**:
    ```bash
    ./mvnw spring-boot:run
    ```

The service will be available at `http://localhost:8080` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

## 📖 API Documentation

Once the service is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

## ⚙️ Configuration

Key environment variables:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port the service runs on | `8080` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `users` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `password` |
| `INIT_DATA` | Whether to initialize sample data | `false` |

## 🛠 Development

### Formatting
This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`

### Database Migrations
Migrations are handled by Flyway. SQL scripts are located in:
`src/main/resources/db/migration`

New migrations should follow the naming convention: `V<Version>__<Description>.sql`.
