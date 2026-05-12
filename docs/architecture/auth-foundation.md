# Auth Foundation

## Purpose
This document defines the Step 4 authentication foundation for the backend.

Scope of this step:
- User entity mapping and repository access
- Password hashing with BCrypt
- Stateless Spring Security setup
- JWT access token generation and validation
- Register/Login/Current User APIs
- Auth-specific error handling conventions

Out of scope:
- Refresh tokens
- OAuth login
- Email verification
- Forgot password
- Workspace authorization and role policies

## Register Flow
1. Normalize input email (`trim` + `lowercase`).
2. Check active user email uniqueness.
3. Hash password using BCrypt.
4. Create user with status `ACTIVE` (MVP baseline).
5. Return `ApiResponse<AuthResponse>` with JWT access token and current user DTO.

## Login Flow
1. Normalize input email.
2. Resolve user by normalized email (excluding soft-deleted records).
3. Verify BCrypt password hash.
4. Enforce status checks:
   - `DISABLED` => forbidden
   - `PENDING` => forbidden
5. Update `last_login_at`.
6. Return `ApiResponse<AuthResponse>` with JWT access token and current user DTO.

Invalid credentials always return the same message:
- `Invalid email or password`

## JWT Flow
- Token type: access token only.
- Signature algorithm: HMAC SHA-256.
- Subject: user id (`UUID`).
- Claims:
  - `email`
  - `status`
  - `type=access`
- Expiration: controlled by environment variable.

No sensitive data (password hash, internal secrets, workspace permissions) is stored in JWT claims.

## Password Hashing Strategy
- Passwords are hashed with `BCryptPasswordEncoder` before persistence.
- Raw passwords are never stored or returned.
- Password hash is never exposed in response DTOs.

## Security Filter Chain
- Session policy: stateless.
- CSRF: disabled for stateless API usage.
- JWT filter parses and validates Bearer tokens.
- Custom authentication entry point returns consistent `ErrorResponse` JSON for unauthorized access.

Public endpoints:
- `GET /api/health`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /actuator/health`
- Swagger endpoints (`/swagger-ui/**`, `/v3/api-docs/**`)

Protected endpoints:
- `GET /api/v1/auth/me`
- Other `/api/**` endpoints by default

## Error Handling
`GlobalExceptionHandler` handles application exceptions with consistent `ErrorResponse`.

Auth foundation error code usage:
- `EMAIL_ALREADY_EXISTS` => `409 Conflict`
- `INVALID_CREDENTIALS` => `401 Unauthorized`
- `USER_DISABLED` => `403 Forbidden`
- `USER_PENDING` => `403 Forbidden`
- `INVALID_TOKEN` => `401 Unauthorized`
- `TOKEN_EXPIRED` => `401 Unauthorized`

Security-layer unauthorized responses are handled by `JwtAuthenticationEntryPoint` using the same `ErrorResponse` shape.

## Environment Variables
Required auth variables:
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`

`JWT_SECRET` must be strong and is expected from environment configuration.
No real secret is committed in repo config files.

## Intentionally Not Implemented Yet
- Refresh token rotation/revocation
- OAuth/social login
- Email verification flows
- Forgot password and reset flows
- Workspace role-based authorization
- Token blacklist/session management

## Future Considerations
- Refresh token + revocation strategy
- Email verification and account activation
- Password reset flows
- Workspace-scoped authorization model
- Security monitoring and anomaly detection
