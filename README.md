# StockPilot API

REST API for product and inventory management, built with Java 21, Spring Boot, Spring Security, Spring Data JPA, Bean Validation, PostgreSQL, and Maven.

## Features

- Health and application info endpoints
- Create, list, find, update, and delete products
- Request validation for product payloads
- Duplicate SKU protection
- Global exception handling
- PostgreSQL persistence
- Automated tests with JUnit, Mockito, and MockMvc
- OpenAPI documentation with Swagger UI

## Architecture

- `product`: product entity, repository, controller, DTOs, and business service
- `common`: domain exceptions and global exception handling
- `config`: Spring Security configuration
- `health`: operational health and info endpoints

The application follows a simple layered architecture: controllers receive HTTP requests, services contain business rules, repositories handle persistence, and DTOs keep the API contract separate from the JPA entity.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/health` | Check API health |
| GET | `/api/v1/info` | Read application information |
| GET | `/api/v1/products` | List products |
| GET | `/api/v1/products/{id}` | Find product by ID |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Delete a product |

Example request:

```json
{
  "name": "Gaming Mouse",
  "sku": "MOU-001",
  "quantity": 15
}
```

## OpenAPI

After starting the application, access:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | API port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/stockpilot` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password for local development |
| `DDL_AUTO` | `update` | Hibernate schema strategy |
| `SHOW_SQL` | `false` | Enables SQL logging |
| `FORMAT_SQL` | `false` | Formats SQL logs |

See `src/main/resources/application-example.properties` for a complete example.

## Running with Docker

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running Tests

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Tests use an in-memory H2 database so they can run locally and in CI without a PostgreSQL dependency.

## Technical Decisions

- PostgreSQL is used as the production-like relational database.
- Database credentials are read from environment variables instead of being hardcoded.
- H2 is used only for automated tests to keep CI fast and reproducible.
- OpenAPI is generated from the Spring MVC controllers with springdoc-openapi.

## Future Improvements

- Add Flyway migrations
- Add pagination and filtering for product listing
- Add authentication strategy aligned with the wider ecosystem
- Add integration tests with Testcontainers
- Add Dockerfile for the API service

## License

This project is licensed under the MIT License.
