# StockPilot API

API REST para gerenciamento de produtos e estoque, desenvolvida com Java e Spring Boot.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Maven
- JUnit 5
- Mockito
- MockMvc

## Funcionalidades

- Health check
- Informações da aplicação
- Criar produto
- Listar produtos
- Buscar produto por ID
- Atualizar produto
- Deletar produto
- Validação de dados
- Tratamento global de erros
- Bloqueio de SKU duplicado
- Testes automatizados

## Endpoints

### Health
`GET /api/v1/health`

### Info
`GET /api/v1/info`

### Criar produto
`POST /api/v1/products`

Exemplo de body:
```json
{
  "name": "Mouse Gamer",
  "sku": "MOU-001",
  "quantity": 15
}