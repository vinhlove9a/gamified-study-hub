# Auth Flow Manual QA (Step 9A Hardening)

Date: 2026-05-28  
Branch: `feat/auth-flow-manual-qa`

## QA account
- fullName: `QA Auth User`
- email: `qa.auth.user@example.com`
- password: `Password123`
- Method: API register/login against local Docker backend.
- This QA account/password is local development test data only, not a production credential or secret.

## Executed checks
- Backend/Frontend startup via Docker:
  - backend started successfully
  - frontend started successfully
  - Flyway validated 3 migrations
  - schema version `3` up to date

- API smoke checks:
  - `POST /api/v1/auth/register` with QA account: `200` success
  - `POST /api/v1/auth/login` with QA account: `200` success
  - `GET /api/v1/auth/me` with Bearer token: `200` success
  - `POST /api/v1/auth/forgot-password` (known email): `200` generic success
  - `POST /api/v1/auth/forgot-password` (unknown email): `200` same generic success (no enumeration)
  - `POST /api/v1/auth/reset-password` (invalid token): `400` controlled API error
  - `POST /api/v1/auth/verify-email` (invalid token): `400` controlled API error

- Route availability checks (HTTP):
  - `/` -> `200`
  - `/auth/login` -> `200`
  - `/auth/register` -> `200`
  - `/auth/forgot-password` -> `200`
  - `/auth/reset-password` -> `200`
  - `/auth/verify-email` -> `200`
  - `/app` -> `200` (SPA fallback reachable)

## Notes
- No code hardening fix was required from API-level QA.
- No frontend/backend source code was changed during this QA step; only QA documentation was added.
- Browser-interaction-only checks still need manual confirmation in a real browser session:
  - unauthenticated `/app` client-side redirect to `/auth/login?redirect=/app`
  - guestOnly redirect from `/auth/login` and `/auth/register` while logged in
  - app-shell logout interaction clearing token/session and returning to `/auth/login`

## Known limitations (expected)
- No refresh token flow.
- No backend email sender.
- No real email link delivery flow for reset/verify.
- Token storage is MVP-level (frontend local storage) and not production-hardened yet.
- `mvn test` may fail in this environment due to Java 25 + Mockito inline MockMaker initialization/self-attach issue (environment-related).

## Step 9C retest and hardening
- Retested API/route flows after UX hardening updates.
- Implemented frontend-only UX hardening fixes:
  - disable auth inputs while submit is in flight (login/register/forgot/reset).
  - preserve async feedback announcements via `aria-live`.
  - guard guestOnly behavior now re-checks session by bootstrap when token exists but in-memory user is missing.
  - app shell logout now has explicit loading/disabled state to avoid repeated clicks.
- No backend API contract changes.
- No refresh token/email sender/business CRUD additions.
- Manual browser interactions that may still need human verification on a local browser:
  - detailed visual/animation smoothness during redirects
  - final UX perception of login->/app and logout->/auth/login transitions
