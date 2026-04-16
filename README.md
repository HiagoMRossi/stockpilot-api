# StockPilot API

Backend API for inventory management and purchase approval workflows.

## Current Status
Project setup completed. Initial Spring Boot structure is running.

## Tech Stack
- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- IntelliJ IDEA
- GitHub Desktop

## Current Endpoints
### Health Check
`GET /api/v1/health`

Example response:
```json
{
  "status": "ok",
  "service": "StockPilot API"
}
