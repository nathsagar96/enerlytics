# Ingestion Service

The **Ingestion Service** is responsible for receiving energy usage data from various sources (devices or simulators)
and publishing it to a Kafka topic for downstream processing. It includes a built-in data simulator for development and
testing purposes.

## 🚀 Core Features

- **Data Ingestion API**: REST endpoint to receive energy usage metrics.
- **Kafka Integration**: Publishes ingestion events to the `energy-usage` topic.
- **Data Simulation**: Configurable background task to simulate device data.
- **Health & Monitoring**: Integrated with Spring Boot Actuator and Prometheus.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Kafka (running via `compose.yaml` at the root)

### Running the Service

1. **Start Infrastructure**: From the project root, start Kafka.
   ```bash
   docker compose -f compose.yaml up -d kafka
   ```

2. **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
   ```bash
   cp .env.example .env
   ```

3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The service will be available at `http://localhost:8082` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

## ⚙️ Configuration

Key environment variables:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port the service runs on | `8082` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9094` |
| `KAFKA_TOPIC` | Kafka topic for usage events | `energy-usage` |
| `SIMULATION_INGESTION_ENDPOINT` | Endpoint for the simulator to hit | `http://localhost:8082/api/v1/ingestions` |
| `SIMULATION_INTERVAL_MS` | Interval between simulation batches | `60000` |
| `SIMULATION_REQUESTS_PER_INTERVAL` | Total requests per interval | `100` |
| `SIMULATION_PARALLEL_THREADS` | Threads for parallel simulation | `2` |

## 🛠 Development

### Formatting

This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`
