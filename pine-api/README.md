# pine-api — Spring Boot API Gateway

The orchestration layer for PineBook. Handles request validation, async job dispatch
to the Python AI service, and structured response aggregation.

## Endpoints

| Method | Path                 | Description                          |
|--------|----------------------|--------------------------------------|
| POST   | `/add-video`         | Submit YouTube URL for processing    |
| POST   | `/ask`               | Query the AI for a learning path     |
| GET    | `/status/{videoId}`  | Check video processing status        |
| GET    | `/actuator/health`   | Health check                         |

## Environment Variables

Copy `.env.example` to `.env` and fill in values.

| Variable              | Description                            |
|-----------------------|----------------------------------------|
| `DB_URL`              | PostgreSQL JDBC URL                    |
| `DB_USERNAME`         | DB username                            |
| `DB_PASSWORD`         | DB password                            |
| `AI_SERVICE_BASE_URL` | Base URL of the Python FastAPI service |
| `AI_SERVICE_TIMEOUT`  | HTTP timeout in milliseconds (default 5000) |

## Running Locally

```bash
./mvnw spring-boot:run
```

Service starts on port **8080**.

## Package Structure

```
com.pinebook.api
├── config/          # WebClient, async executor configuration
├── controller/      # Thin REST controllers
├── service/         # Business logic + AI client calls
├── model/           # JPA entities
├── repository/      # Spring Data JPA repositories
├── dto/             # Request/response DTOs
├── exception/       # Global exception handler
└── util/            # Correlation ID filter
```
