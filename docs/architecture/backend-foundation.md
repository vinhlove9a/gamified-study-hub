# Backend Foundation

## Purpose
This document defines the backend foundation layer for Gamified Study Hub.

Scope in this phase:
- Consistent API response wrappers
- Centralized error handling
- Base persistence/auditing abstractions
- Flyway baseline setup
- OpenAPI documentation exposure
- Development CORS baseline

Out of scope:
- Authentication/authorization
- Business/domain modules
- Tenant/workspace rules
- Document/coin/mission logic

## Response Convention
Application endpoints return `ApiResponse<T>`:
- `success`: operation status
- `message`: human-readable summary
- `data`: response payload
- `timestamp`: server timestamp (`Instant`)

Paginated results should use `PageResponse<T>` for page metadata consistency.

## Error Handling Convention
All API exceptions are translated by `GlobalExceptionHandler` into `ErrorResponse`:
- `success`: always `false`
- `code`: stable machine-readable error code
- `message`: safe error summary
- `path`: request path
- `timestamp`: server timestamp
- `fieldErrors`: populated for validation errors

Custom business-style runtime exceptions:
- `BusinessException`
- `ResourceNotFoundException`
- `UnauthorizedException`
- `ForbiddenException`

No internal stack traces are exposed in API responses.

## BaseEntity and Auditing Strategy
`BaseEntity` is a `@MappedSuperclass` with:
- `UUID id`
- `Instant createdAt`
- `Instant updatedAt`
- `Instant deletedAt` (nullable for future soft-delete)

Auditing is enabled via `@EnableJpaAuditing` and `AuditingEntityListener`.

## Flyway Baseline Strategy
Flyway is enabled for dev/prod profiles with migration location:
- `classpath:db/migration`

Current baseline migration:
- `V1__baseline.sql`

This migration establishes foundation only and intentionally does not create business tables.

## OpenAPI Setup
Springdoc OpenAPI is enabled.

Available endpoints:
- `/v3/api-docs`
- `/swagger-ui.html`
- `/swagger-ui/index.html`

Configured API metadata:
- Title: `Gamified Study Hub API`
- Version: `v0.1.0`
- Description: `API documentation for Gamified Study Hub backend`

## Intentionally Not Implemented Yet
- Security schemes (JWT/auth)
- Module-specific controller/service/repository layers
- Business entities and migrations
- Multi-tenant resolution logic
