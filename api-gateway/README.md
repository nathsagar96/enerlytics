# API Gateway

The **API Gateway** is the entry point for the Enerlytics ecosystem. It handles routing, security, and resiliency (
circuit breaking) for all backend microservices. Built with Spring Cloud Gateway MVC, it provides a centralized way to
access the system's APIs and documentation.

## 🚀 Core Features

- **Centralized Routing**: Routes requests to appropriate microservices (User, Device, Ingestion, etc.).
- **Security**: OAuth2 Resource Server integration with Keycloak.
- **Resiliency**: Circuit breaker patterns using Resilience4j to prevent cascading failures.
- **Unified Documentation**: Aggregates Swagger/OpenAPI documentation from all microservices.
- **CORS Support**: Configured for secure cross-origin resource sharing.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0 & Spring Cloud Gateway MVC
- **Security**: Spring Security OAuth2
- **Resiliency**: Resilience4j
- **Documentation**: Springdoc OpenAPI
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Running Keycloak instance (configured in `compose.yaml` at the root)

### Running the Gateway

1. **Start Infrastructure**: Ensure Keycloak and other dependencies are running.
   ```bash
   docker compose -f compose.yaml up -d
   ```

2. **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
   ```bash
   cp .env.example .env
   ```

3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The gateway will be available at `http://localhost:9000`.

### Running Tests

```bash
./mvnw clean test
```

## 📖 API Documentation

The gateway provides a unified Swagger UI to explore all microservice APIs:

- **Unified Swagger UI**: `http://localhost:9000/swagger-ui.html`
- **Gateway Health**: `http://localhost:9000/actuator/health`

## ⚙️ Configuration

Key environment variables:

| Variable                | Description                  | Default                     |
|:------------------------|:-----------------------------|:----------------------------|
| `SERVER_PORT`           | Port the gateway runs on     | `9000`                      |
| `JWK_SET_URI`           | Keycloak JWK set URI         | `http://localhost:8091/...` |
| `USER_SERVICE_URL`      | URL of the User Service      | `http://localhost:8080`     |
| `DEVICE_SERVICE_URL`    | URL of the Device Service    | `http://localhost:8081`     |
| `INGESTION_SERVICE_URL` | URL of the Ingestion Service | `http://localhost:8082`     |
| `INSIGHT_SERVICE_URL`   | URL of the Insight Service   | `http://localhost:8085`     |

## 🛠 Development

### Formatting

This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`
