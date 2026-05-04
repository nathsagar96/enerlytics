# Alert Service

The **Alert Service** is a critical component of the Enerlytics platform, responsible for processing energy usage alerts and notifying users via email. It listens to alert events from Kafka and persists notification history in PostgreSQL.

## 🚀 Core Features

- **Alert Processing**: Consumes `energy-alerts` from Kafka.
- **Email Notifications**: Sends alerts using SMTP (integrated with Mailpit for local development).
- **Audit Logging**: Persists alert history in the database.
- **Resilient Delivery**: Robust error handling for mail delivery and event processing.
- **Health & Monitoring**: Integrated with Spring Boot Actuator and Prometheus.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0
- **Messaging**: Apache Kafka
- **Database**: PostgreSQL
- **Migration**: Flyway
- **Email**: Spring Boot Starter Mail
- **Build Tool**: Maven
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for running PostgreSQL, Kafka, and Mailpit via `compose.yaml` at the root)

### Running the Service

1.  **Start Infrastructure**: From the project root, start the necessary containers.
    ```bash
    docker compose -f compose.yaml up -d alerts-db kafka mailpit
    ```

2.  **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
    ```bash
    cp .env.example .env
    ```

3.  **Run Application**:
    ```bash
    ./mvnw spring-boot:run
    ```

The service will be available at `http://localhost:8084` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

## ⚙️ Configuration

Key environment variables:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port the service runs on | `8084` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `alerts` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9094` |
| `MAIL_HOST` | SMTP server host | `localhost` |
| `MAIL_PORT` | SMTP server port | `1025` |

## 🛠 Development

### Formatting
This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`

### Database Migrations
Migrations are handled by Flyway. SQL scripts are located in:
`src/main/resources/db/migration`
