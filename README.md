# Enerlytics

A cloud-native energy analytics platform built with a microservices' architecture. Enerlytics enables real-time
tracking,
analysis, and intelligent insights for smart energy devices — from data ingestion through AI-powered recommendations.

## 📐 Architecture Overview

```
                           ╔══════════════╗
                           ║  API Gateway ║
                           ║  OAuth2/JWT  ║
                           ║    :9000     ║
                           ╚══════╤═══════╝
         ┌────────────┬───────────┴───────────┬────────────┐
         ▼            ▼                       ▼            ▼
  ╔════════════╗╔════════════╗         ╔════════════╗╔════════════╗
  ║    User    ║║   Device   ║         ║ Ingestion  ║║  Insight   ║
  ║  Service   ║║  Service   ║         ║  Service   ║║  Service   ║
  ║   :8080    ║║   :8081    ║         ║   :8082    ║║   :8085    ║
  ╚═════╤══════╝╚═════╤══════╝         ╚═════╤══════╝╚═════╤══════╝
        │             │                      │             │
        │             │              ┌───────▼───────┐     │
        │             │              │     Kafka     │     │
        │             │              │ [energy-usage]│     │
        │             │              └───────┬───────┘     │
        │             │              ╔═══════▼═══════╗     │
        │             │              ║     Usage     ║◀────┤
        │             │              ║    Service    ║     │ ╔══════════╗
        │             │              ║     :8083     ║     └▶║  Ollama  ║
        │             │              ╚═══╤═════════╤═╝       ║   (AI)   ║
        │             │                  │         │         ╚══════════╝
        │             │          ┌───────▼───────┐ │           ┌───────────┐
        │             │          │     Kafka     │ └──────────▶│ InfluxDB  │
        │             │          │[energy-alerts]│             │ (metrics) │
        │             │          └───────┬───────┘             └───────────┘
        │             │          ╔═══════▼═══════╗
        │             │          ║     Alert     ║
        │             │          ║    Service    ║
        │             │          ║     :8084     ║
        │             │          ╚═══╤═══════╤═══╝
        │             │              │       │
  ┌─────▼─────────────▼──────────────▼───┐ ┌─▼──────────┐
  │              PostgreSQL              │ │  Mailpit   │
  │            (Relational DB)           │ │   :8025    │
  └──────────────────────────────────────┘ └────────────┘
```

## 🧩 Services

| Service                                     | Port   | Description                                                                       | Key Dependencies           |
|:--------------------------------------------|:-------|:----------------------------------------------------------------------------------|:---------------------------|
| [**API Gateway**](api-gateway/)             | `9000` | Centralized routing, OAuth2 security, circuit breaking, and aggregated Swagger UI | Keycloak                   |
| [**User Service**](user-service/)           | `8080` | User profile management and alerting preferences                                  | PostgreSQL                 |
| [**Device Service**](device-service/)       | `8081` | Smart device lifecycle management and user-device associations                    | PostgreSQL                 |
| [**Ingestion Service**](ingestion-service/) | `8082` | Receives energy usage data and publishes to Kafka                                 | Kafka                      |
| [**Usage Service**](usage-service/)         | `8083` | Processes, aggregates, and monitors energy consumption data                       | Kafka, InfluxDB            |
| [**Alert Service**](alert-service/)         | `8084` | Consumes alert events from Kafka and sends email notifications                    | Kafka, PostgreSQL, Mailpit |
| [**Insight Service**](insight-service/)     | `8085` | AI-powered energy-saving tips and usage overviews                                 | Ollama                     |

## 🛠 Tech Stack

- **Language**: Java 25
- **Framework**: Spring Boot 3.5 / 4.0
- **Gateway**: Spring Cloud Gateway MVC
- **Security**: Keycloak (OAuth2 / OpenID Connect)
- **Databases**: PostgreSQL, InfluxDB
- **Messaging**: Apache Kafka
- **AI**: Spring AI + Ollama
- **Monitoring**: Prometheus + Grafana
- **Email (Dev)**: Mailpit
- **Migrations**: Flyway
- **Code Style**: Spotless (Palantir Java Format)
- **Build**: Maven

## 🏁 Quick Start

### Prerequisites

- **JDK 25**
- **Maven 3.9+**
- **Docker** & **Docker Compose**
- **Ollama** (only for the Insight Service)

### 1. Start Infrastructure

From the repository root, spin up all required dependencies:

```bash
docker compose -f compose.yaml up -d
```

This starts PostgreSQL, Kafka, InfluxDB, Mailpit, and Keycloak.

> [!TIP]
> You can start only the services you need, e.g. `docker compose up -d postgres kafka` if you're only working on the
> Ingestion Service.

### 2. Run a Service

Navigate into any service directory and run:

```bash
cd user-service
cp .env.example .env      # adjust if needed
./mvnw spring-boot:run
```

### 3. Explore the APIs

Some of the service exposes Swagger UI for interactive API exploration:

| Service                  | Swagger UI                                |
|:-------------------------|:------------------------------------------|
| User Service             | http://localhost:8080/swagger-ui.html     |
| Device Service           | http://localhost:8081/swagger-ui.html     |
| Ingestion Service        | http://localhost:8082/swagger-ui.html     |
| Insight Service          | http://localhost:8085/swagger-ui.html     |
| **Aggregated (Gateway)** | **http://localhost:9000/swagger-ui.html** |

## 🗄 Infrastructure

### Docker Compose Services

| Container   | Image                    | Exposed Port                | Purpose                                                 |
|:------------|:-------------------------|:----------------------------|:--------------------------------------------------------|
| `postgres`  | `postgres:18-alpine`     | `5432`                      | Relational storage for User, Device, and Alert services |
| `kafka`     | `apache/kafka:4.2.0`     | `9094` (external)           | Event streaming between services                        |
| `usages-db` | `influxdb:2.7-alpine`    | `8072`                      | Time-series storage for energy usage data               |
| `mailpit`   | `axllent/mailpit:latest` | `8025` (UI) / `1025` (SMTP) | Email testing — captures all outgoing mail              |
| `keycloak`  | `keycloak:26.4`          | `8091`                      | Identity and access management (OAuth2/OIDC)            |

#### Optional Profiles

```bash
# Developer tools — Kafka UI
docker compose --profile tools up -d

# Monitoring stack — Prometheus + Grafana
docker compose --profile monitoring up -d
```

| Tool       | URL                   | Profile      |
|:-----------|:----------------------|:-------------|
| Kafka UI   | http://localhost:8070 | `tools`      |
| Prometheus | http://localhost:9090 | `monitoring` |
| Grafana    | http://localhost:3000 | `monitoring` |
| Mailpit UI | http://localhost:8025 | *(default)*  |

### Database Initialization

The `postgres-init/init.sql` script automatically creates the required databases (`users`, `devices`, `alerts`,
`keycloak`) on first run. Schema migrations within each service are handled by **Flyway**.

## 🧪 Development

### Build & Test

Commands are run from within each service directory:

```bash
# Run tests
./mvnw test

# Full verification (compile + test + checks)
./mvnw clean verify

# Check code formatting
./mvnw spotless:check

# Auto-format code
./mvnw spotless:apply
```

### Project Structure

```
enerlytics/
├── api-gateway/           # Centralized API gateway
├── user-service/          # User management
├── device-service/        # Device management
├── ingestion-service/     # Data ingestion & simulation
├── usage-service/         # Usage processing & aggregation
├── alert-service/         # Alert processing & email
├── insight-service/       # AI-powered insights
├── monitoring/            # Prometheus & Grafana configs
│   ├── prometheus/
│   └── grafana/
├── postgres-init/         # Database initialization scripts
├── compose.yaml           # Local infrastructure
└── AGENTS.md              # Repository guidelines
```

### Conventions

- **Java 25** across all services
- **Spotless** enforces formatting — run `./mvnw spotless:apply` before committing
- **Flyway** for database migrations (`src/main/resources/db/migration`)
- Test classes follow the `*Test` naming convention
- Package structure: `com.enerlytics.<domain>`
- Class naming: `*Controller`, `*Service`, `*Repository`, `*Request`, `*Response`

### Integration Testing

Services with database dependencies use **Testcontainers** for integration tests. A managed PostgreSQL container is spun
up automatically — no manual database setup required.

```bash
./mvnw test    # Testcontainers starts/stops containers automatically
```

## 📄 License

This project is licensed under the [MIT License](LICENSE).