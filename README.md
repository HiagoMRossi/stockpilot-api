# StockPilot API

[![CI](https://github.com/HiagoMRossi/stockpilot-api/actions/workflows/ci.yml/badge.svg)](https://github.com/HiagoMRossi/stockpilot-api/actions/workflows/ci.yml)

REST API for product and inventory management, built with Java 21, Spring Boot, Spring Security, Spring Data JPA, Bean Validation, PostgreSQL, and Maven.

StockPilot helps small teams track products, stock levels, low-stock alerts, and inventory adjustments through a documented REST API.

## Features

- Health and application info endpoints
- Create, list, search, find, update, and delete products
- Product price and category fields
- Per-product low-stock threshold and low-stock alert flag
- Low-stock product listing
- Stock adjustment endpoint for inventory movements
- Paginated product listing with name/SKU search
- Request validation for product payloads
- Duplicate SKU protection
- Flyway database migrations
- Global exception handling
- PostgreSQL persistence
- Service and controller tests with JUnit, Mockito, H2, and MockMvc
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
| GET | `/api/v1/products?page=0&size=10&search=mouse` | List products with pagination and optional name/SKU search |
| GET | `/api/v1/products/low-stock` | List products where `quantity <= lowStockThreshold` |
| GET | `/api/v1/products/{id}` | Find product by ID |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| PATCH | `/api/v1/products/{id}/stock` | Adjust product stock by a positive or negative quantity change |
| DELETE | `/api/v1/products/{id}` | Delete a product |

Example request:

```json
{
  "name": "Gaming Mouse",
  "sku": "MOU-001",
  "quantity": 15,
  "price": 199.90,
  "category": "Peripherals",
  "lowStockThreshold": 5
}
```

Example stock adjustment:

```json
{
  "quantityChange": -3
}
```

Example paginated response:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Gaming Mouse",
      "sku": "MOU-001",
      "quantity": 4,
      "price": 199.90,
      "category": "Peripherals",
      "lowStockThreshold": 5,
      "lowStock": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
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
| `DDL_AUTO` | `validate` | Hibernate schema strategy |
| `FLYWAY_ENABLED` | `true` | Enables Flyway database migrations |
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
- Flyway owns schema migrations and Hibernate validates the mapped schema.
- H2 is used only for automated tests to keep CI fast and reproducible; tests execute the same Flyway migrations.
- Product listing returns a stable pagination DTO instead of exposing Spring's internal `Page` serialization shape.
- Low-stock alerts are calculated per product using `quantity <= lowStockThreshold`.
- Stock adjustments are delta-based: positive values add stock and negative values remove stock, but the API rejects adjustments that would make stock negative.
- OpenAPI is generated from the Spring MVC controllers with springdoc-openapi.

## Future Improvements

- Add authentication strategy aligned with the wider ecosystem
- Add integration tests with Testcontainers
- Add Dockerfile for the API service

## License

This project is licensed under the MIT License.
