# Backend (Spring Boot)

Base backend service for Gamified Study Hub.

## Scope in This Phase
Implemented:
- Spring Boot application bootstrap
- Environment profile configuration
- PostgreSQL/Flyway connectivity configuration
- `/api/health` endpoint
- Basic context-load test

Not implemented yet:
- Authentication/authorization
- Business/domain modules
- Database business schema

## Local Run
```bash
mvn spring-boot:run
```

## Test
```bash
mvn test
```

## Health Check
```bash
curl http://localhost:8080/api/health
```
