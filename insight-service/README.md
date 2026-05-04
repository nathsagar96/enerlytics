# Insight Service

The **Insight Service** is an AI-powered microservice in the Enerlytics ecosystem, responsible for generating
personalized energy-saving tips and usage overviews. It leverages the Ollama chat model to analyze energy consumption
data and provide actionable insights to users.

## 🚀 Core Features

- **Personalized Saving Tips**: AI-generated suggestions based on recent energy consumption.
- **Usage Overview**: Concise summaries of energy usage patterns.
- **AI Integration**: Powered by Ollama and Spring AI.
- **API Documentation**: Interactive Swagger/OpenAPI UI.
- **Health & Monitoring**: Integrated with Spring Boot Actuator and Prometheus.

## 🛠 Tech Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 3.5
- **AI Integration**: Spring AI with Ollama
- **Build Tool**: Maven
- **Documentation**: Springdoc OpenAPI
- **Formatting**: Spotless (Palantir Java Format)

## 🏁 Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- Ollama (running locally with `qwen2.5-coder:0.5b` or configured model)

### Running the Service

1. **Ensure Ollama is running**:
   ```bash
   ollama run qwen2.5-coder:0.5b
   ```

2. **Environment Setup**: Copy `.env.example` to `.env` and adjust if necessary.
   ```bash
   cp .env.example .env
   ```

3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The service will be available at `http://localhost:8085` (or the port specified in `SERVER_PORT`).

### Running Tests

```bash
./mvnw clean test
```

## 📖 API Documentation

Once the service is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8085/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8085/v3/api-docs`

## ⚙️ Configuration

Key environment variables:

| Variable            | Description               | Default                               |
|:--------------------|:--------------------------|:--------------------------------------|
| `SERVER_PORT`       | Port the service runs on  | `8085`                                |
| `OLLAMA_BASE_URL`   | Ollama API base URL       | `http://localhost:11434`              |
| `OLLAMA_MODEL`      | AI model to use           | `qwen2.5-coder:0.5b`                  |
| `USAGE_SERVICE_URL` | URL for the usage service | `http://localhost:8083/api/v1/usages` |

## 🛠 Development

### Formatting

This project uses **Spotless** to enforce consistent coding style.

- **Check formatting**: `./mvnw spotless:check`
- **Apply formatting**: `./mvnw spotless:apply`
