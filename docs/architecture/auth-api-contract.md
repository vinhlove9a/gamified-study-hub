# Auth API Contract (Design Phase)

## Scope
- This document defines the API contract for auth integration between frontend auth UI and backend.
- This is design/documentation only.
- No backend auth business implementation is added in this step.

## Current backend foundation (inspected)
- Existing endpoints:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
- Security:
  - Stateless JWT auth via `Authorization: Bearer <token>`.
  - `BCryptPasswordEncoder` configured.
  - Public: `/api/health`, `/api/v1/auth/register`, `/api/v1/auth/login`.
  - Protected: `/api/v1/auth/me`, all other `/api/**`.
- JWT behavior:
  - Access token only (`type=access` claim).
  - Claims: `sub=userId`, `email`, `status`, `type=access`, `iat`, `exp`.
  - Expiration from `app.jwt.access-token-expiration-minutes` (default `60`).
- User schema (`users`):
  - `id`, `email`, `password_hash`, `full_name`, `avatar_url`, `status`, `last_login_at`, `created_at`, `updated_at`, `deleted_at`.
  - `status`: `ACTIVE | DISABLED | PENDING`.
  - No `email_verified` column yet.
  - No auth token persistence tables yet.
- Error envelope (current standard):
  - `ErrorResponse`: `success`, `code`, `message`, `path`, `timestamp`, `fieldErrors[]`.

## API base
- Base path: `/api/v1/auth`
- Content type: `application/json`

## Standard user summary
- Use current + planned-safe fields only:
```json
{
  "id": "uuid",
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "status": "ACTIVE",
  "emailVerified": false,
  "createdAt": "2026-05-27T08:00:00Z",
  "updatedAt": "2026-05-27T08:00:00Z"
}
```
- Notes:
  - `emailVerified` is planned and should default to `false` until schema adds this field.
  - Never expose `passwordHash`.

## Endpoint contract

### 1) POST `/api/v1/auth/register`
- Purpose: create account and trigger verification flow.
- Request:
```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```
- Success `201 Created`:
```json
{
  "success": true,
  "message": "Đăng ký thành công.",
  "data": {
    "user": {
      "id": "uuid",
      "fullName": "Nguyen Van A",
      "email": "user@example.com",
      "status": "PENDING",
      "emailVerified": false,
      "createdAt": "2026-05-27T08:00:00Z",
      "updatedAt": "2026-05-27T08:00:00Z"
    },
    "emailVerificationRequired": true
  },
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`
  - `409` `AUTH_EMAIL_ALREADY_EXISTS`
  - `400` `AUTH_PASSWORD_WEAK`

### 2) POST `/api/v1/auth/login`
- Request:
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```
- Success `200 OK`:
```json
{
  "success": true,
  "message": "Đăng nhập thành công.",
  "data": {
    "accessToken": "jwt",
    "tokenType": "Bearer",
    "expiresInSeconds": 3600,
    "user": {
      "id": "uuid",
      "fullName": "Nguyen Van A",
      "email": "user@example.com",
      "status": "ACTIVE",
      "emailVerified": true,
      "createdAt": "2026-05-27T08:00:00Z",
      "updatedAt": "2026-05-27T08:00:00Z"
    }
  },
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`
  - `401` `AUTH_INVALID_CREDENTIALS`
  - `403` `AUTH_EMAIL_NOT_VERIFIED`
  - `403` `AUTH_ACCOUNT_DISABLED`

### 3) POST `/api/v1/auth/forgot-password`
- Request:
```json
{
  "email": "user@example.com"
}
```
- Success `200 OK` (generic, anti-enumeration):
```json
{
  "success": true,
  "message": "Nếu email tồn tại, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.",
  "data": {},
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`

### 4) POST `/api/v1/auth/reset-password`
- Request:
```json
{
  "token": "reset-token",
  "newPassword": "NewPassword123!"
}
```
- Success `200 OK`:
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công.",
  "data": {},
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`
  - `400` `AUTH_PASSWORD_WEAK`
  - `400` `AUTH_PASSWORD_RESET_TOKEN_INVALID`
  - `400` `AUTH_EXPIRED_TOKEN`

### 5) POST `/api/v1/auth/verify-email`
- Request:
```json
{
  "token": "verify-token"
}
```
- Success `200 OK`:
```json
{
  "success": true,
  "message": "Xác thực email thành công.",
  "data": {
    "user": {
      "id": "uuid",
      "fullName": "Nguyen Van A",
      "email": "user@example.com",
      "status": "ACTIVE",
      "emailVerified": true,
      "createdAt": "2026-05-27T08:00:00Z",
      "updatedAt": "2026-05-27T08:00:00Z"
    }
  },
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`
  - `400` `AUTH_EMAIL_VERIFICATION_TOKEN_INVALID`
  - `400` `AUTH_EXPIRED_TOKEN`

### 6) POST `/api/v1/auth/resend-verification`
- Request:
```json
{
  "email": "user@example.com"
}
```
- Success `200 OK` (generic, anti-enumeration):
```json
{
  "success": true,
  "message": "Nếu email tồn tại và chưa xác thực, chúng tôi đã gửi lại email xác thực.",
  "data": {},
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `400` `VALIDATION_ERROR`

### 7) GET `/api/v1/auth/me` (authenticated)
- Header: `Authorization: Bearer <accessToken>`
- Success `200 OK`:
```json
{
  "success": true,
  "message": "Thông tin người dùng hiện tại.",
  "data": {
    "id": "uuid",
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "status": "ACTIVE",
    "emailVerified": true,
    "createdAt": "2026-05-27T08:00:00Z",
    "updatedAt": "2026-05-27T08:00:00Z"
  },
  "timestamp": "2026-05-27T08:00:00Z"
}
```
- Errors:
  - `401` `UNAUTHORIZED`
  - `401` `AUTH_INVALID_TOKEN`
  - `401` `AUTH_EXPIRED_TOKEN`

### Deferred endpoints for first integration
- `POST /api/v1/auth/refresh-token`:
  - Defer. Current architecture has no refresh token schema/persistence.
- `POST /api/v1/auth/logout`:
  - Defer. Current architecture has no token invalidation/blacklist strategy.

## Error model
- Standard error body (compatible with current backend):
```json
{
  "success": false,
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "Email hoặc mật khẩu không đúng.",
  "path": "/api/v1/auth/login",
  "timestamp": "2026-05-27T08:00:00Z",
  "fieldErrors": []
}
```
- For validation:
```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/auth/register",
  "timestamp": "2026-05-27T08:00:00Z",
  "fieldErrors": [
    {
      "field": "password",
      "message": "Password must be between 8 and 100 characters",
      "rejectedValue": "123"
    }
  ]
}
```

## Error code catalog (contract)
- `AUTH_INVALID_CREDENTIALS`
- `AUTH_EMAIL_ALREADY_EXISTS`
- `AUTH_EMAIL_NOT_VERIFIED`
- `AUTH_ACCOUNT_DISABLED`
- `AUTH_INVALID_TOKEN`
- `AUTH_EXPIRED_TOKEN`
- `AUTH_PASSWORD_WEAK`
- `AUTH_PASSWORD_RESET_TOKEN_INVALID`
- `AUTH_EMAIL_VERIFICATION_TOKEN_INVALID`
- `VALIDATION_ERROR`
- `UNAUTHORIZED`

## Mapping note to current constants
- Existing constants are currently:
  - `INVALID_CREDENTIALS`, `EMAIL_ALREADY_EXISTS`, `USER_DISABLED`, `USER_PENDING`, `INVALID_TOKEN`, `TOKEN_EXPIRED`.
- Implementation phase should either:
  - introduce `AUTH_*` constants as canonical external contract, or
  - map internal constants to these API contract codes in responses.

## Security decisions
- Password hashing: BCrypt.
- JWT strategy (phase 1): access-token only.
- Forgot password + resend verification: always return generic success to prevent account enumeration.
- Email verification: may use mocked/local delivery in first implementation if email provider is not ready.
- Public endpoints:
  - `POST /register`, `POST /login`, `POST /forgot-password`, `POST /reset-password`, `POST /verify-email`, `POST /resend-verification`.
- Protected endpoint:
  - `GET /me`.
- CORS:
  - Backend should allow frontend origin from `FRONTEND_ORIGIN` (current default `http://localhost:5173`).
- No social login/OAuth in this phase.

## Token strategy decisions
- Access token:
  - Signed JWT HS256 using `app.jwt.secret`.
  - Expiration controlled by `app.jwt.access-token-expiration-minutes`.
- Verification/reset tokens:
  - Prefer opaque random tokens stored hashed in DB (safer than long-lived JWT for one-time flows).
  - One-time use, short TTL, revoke on consumption.

## Database impact
- Applied migration:
  - `V3__auth_email_verification_and_reset_tokens.sql`
- Added to `users`:
  - `email_verified BOOLEAN NOT NULL DEFAULT FALSE`
  - `email_verified_at TIMESTAMPTZ NULL`
- Added tables:
  - `auth_email_verification_tokens`
  - `auth_password_reset_tokens`
- Token storage rules:
  - only `token_hash` is persisted
  - raw token values are never stored in database
  - `consumed_at` marks one-time token usage
- Referential behavior:
  - token tables use `ON DELETE CASCADE` to remove orphaned auth tokens when a user is deleted
- Deferred:
  - refresh token table remains deferred (`V4__auth_refresh_tokens.sql` when refresh-token feature is enabled)

## Frontend integration mapping
- `LoginPage` -> `POST /api/v1/auth/login`
- `RegisterPage` -> `POST /api/v1/auth/register`
- `ForgotPasswordPage` -> `POST /api/v1/auth/forgot-password`
- `ResetPasswordPage` -> `POST /api/v1/auth/reset-password`
- `VerifyEmailPage` -> `POST /api/v1/auth/verify-email`
- `AuthNoticePage` / `AuthErrorPage`:
  - Map backend result codes to route `type` query for UX states.

## Out of scope in this phase
- Controller/service/repository business implementation.
- Email provider integration details.
- Refresh token persistence and logout invalidation logic.

## Implementation status (backend foundation)
- DTOs created/aligned for contract:
  - `RegisterRequest`, `LoginRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`, `VerifyEmailRequest`, `ResendVerificationRequest`
  - `UserSummaryResponse`, `AuthResponse`, `AuthMessageResponse`
- Token persistence foundation created:
  - `EmailVerificationToken` entity + repository
  - `PasswordResetToken` entity + repository
- Service foundation helpers added:
  - `AuthMapper` for `User -> UserSummaryResponse` and `AuthResponse` mapping
  - `PasswordPolicyValidator` with minimum password policy (>=8 chars, at least one letter, one number)
  - `AuthTokenService` for opaque URL-safe token generation, SHA-256 hashing, and token persistence helpers
- Service-layer auth flow methods implemented in `AuthService`:
  - `forgotPassword(ForgotPasswordRequest)` with anti-enumeration generic response
  - `resetPassword(ResetPasswordRequest)` with token consume + password update
  - `verifyEmail(VerifyEmailRequest)` with token consume + `emailVerified` update
  - `resendVerification(ResendVerificationRequest)` with anti-enumeration generic response
  - these methods are internal service layer only; no new controllers/endpoints exposed yet
- Existing register/login/me foundation remains operational with `UserSummaryResponse`.
- Not implemented yet:
  - email sending integration
  - frontend integration wiring for forgot/reset/verify/resend
  - refresh token flow
