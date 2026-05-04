# Usage Service

The **Usage Service** is the analytical engine of the Enerlytics ecosystem. It processes real-time energy consumption
data, aggregates it using InfluxDB, and monitors usage against user-defined thresholds to trigger alerts.

## 🚀 Core Features

- **Real-time Data Processing**: Consumes energy usage events from Kafka and stores them in InfluxDB.
- **Usage Aggregation**: Periodically aggregates device-level energy consumption.
- **Threshold Monitoring**: Compares aggregated usage against user preferences and triggers alerts via Kafka.
- **Historical Analysis**: Provides APIs to retrieve aggregated usage data over custom time periods.
- **Service Integration**: Orchestrates data from `Device Service` and `User Service` to provide context to energy
  metrics.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0
- **Time-series Database**: InfluxDB
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for running InfluxDB and Kafka via `compose.yaml` at the root)

### Running the Service

1. **Start Infrastructure**: From the project root, start the required dependencies.
   ```bash
   docker compose -f compose.yaml up -d influxdb kafka
   ```

2. **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
   ```bash
   cp .env.example .env
   ```

3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The service will be available at `http://localhost:8083` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

## ⚙️ Configuration

Key environment variables:

| Variable                  | Description                   | Default                                |
|:--------------------------|:------------------------------|:---------------------------------------|
| `SERVER_PORT`             | Port the service runs on      | `8083`                                 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers       | `localhost:9094`                       |
| `INFLUX_URL`              | InfluxDB connection URL       | `http://localhost:8072`                |
| `INFLUX_TOKEN`            | InfluxDB authentication token | `my-token`                             |
| `INFLUX_ORG`              | InfluxDB organization         | `enerlytics`                           |
| `INFLUX_BUCKET`           | InfluxDB bucket for usages    | `usages`                               |
| `USER_SERVICE_URL`        | Base URL for User Service     | `http://localhost:8080/api/v1/users`   |
| `DEVICE_SERVICE_URL`      | Base URL for Device Service   | `http://localhost:8081/api/v1/devices` |

## 🛠 Development

### Formatting

This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`
