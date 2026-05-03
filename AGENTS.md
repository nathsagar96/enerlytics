# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-service Java backend. Each service is an independent Maven project with its own `pom.xml`, `mvnw`, and `src/` tree:
- `user-service`, `device-service`, `usage-service`, `ingestion-service`, `alert-service`, `insight-service`, `api-gateway`
- Code: `src/main/java/com/enerlytics/...`
- Config: `src/main/resources/application.yaml`
- Tests: `src/test/java/...`
- DB migrations (where applicable): `src/main/resources/db/migration`

Shared local infrastructure lives in [`compose.yaml`](compose.yaml) (Postgres, Kafka, InfluxDB, Mailpit).

## Build, Test, and Development Commands
Run commands from the target service directory.
- `./mvnw spring-boot:run`: Start a service locally.
- `./mvnw test`: Run unit/integration tests for that service.
- `./mvnw clean verify`: Full compile + test + verification cycle.
- `./mvnw spotless:check`: Validate Java formatting.
- `./mvnw spotless:apply`: Auto-format code.

From repo root:
- `docker compose -f compose.yaml up -d`: Start local dependencies.
- `docker compose -f compose.yaml down`: Stop dependencies.

## Coding Style & Naming Conventions
- Java 25 is used across services.
- Formatting is enforced with Spotless (`palantirJavaFormat`).
- Use 4-space indentation and standard Spring naming:
  - `*Controller`, `*Service`, `*Repository`, `*Request`, `*Response`.
- Keep package paths under `com.enerlytics.<domain>`.
- Prefer clear, feature-scoped class names (for example, `UserController`, `DeviceUsageResponse`).

## Testing Guidelines
- Primary test stack: Spring Boot test starters + JUnit 5.
- Test classes should end with `Test` (for example, `UserServiceTest`, `DeviceControllerTest`).
- Add/update tests for every behavior change, especially controller contracts, service logic, and exception handling.
- Run `./mvnw test` in every touched service before opening a PR.

## Commit & Pull Request Guidelines
Commit history favors short, imperative messages (for example, `Initialize usage-service...`, `fix typo in properties...`).
- Keep commits focused by service or concern.
- Use present-tense, imperative summaries; include scope when useful (for example, `api-gateway: add resilience4j circuit breaker`).

PRs should include:
- What changed and why.
- Services affected.
- Setup or migration notes (ports, env vars, Flyway scripts).
- API examples or screenshots for behavior/UI changes (Mailpit/Kafka UI where relevant).
