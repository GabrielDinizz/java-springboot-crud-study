# first-spring-app

API REST de tarefas (CRUD) construída com Spring Boot, como projeto de estudo — ponto de partida para quem já conhece Laravel e está aprendendo o ecossistema Spring.

## Stack

- Java 21
- Spring Boot (Web, Data JPA, Validation)
- H2 (banco em memória)
- springdoc-openapi / Swagger UI
- Lombok

## Rodando o projeto

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Console do H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tarefasdb`)

## Estrutura

```
src/main/java/com/pessoal/first_spring_app
├── controller/   → endpoints HTTP
├── service/      → regras de negócio
├── repository/   → acesso ao banco (Spring Data JPA)
├── model/        → entidades JPA
├── dto/          → contratos de entrada/saída da API
├── exception/    → exceções customizadas + tratamento global
└── config/       → configuração manual (beans)
```

## Documentação

[Do Laravel ao Spring Boot — guia da API de Tarefas](docs/laravel-vs-spring.md): guia comparativo explicando cada camada do projeto (entidade, repository, DTO, service, controller, validação, tratamento de exceções) lado a lado com o equivalente em Laravel, incluindo perguntas de entrevista e uma cola rápida de termos.
