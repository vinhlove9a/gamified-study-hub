# Local Setup

## Prerequisites
- Java 21
- Maven 3.9+
- Node.js LTS (recommended 20+)
- Docker
- Docker Compose

## 1) Prepare Environment File
From repository root:

```bash
cp .env.example .env
```

Update values only if your local ports or credentials need changes.
For auth foundation, make sure `JWT_SECRET` is set with a strong value (at least 32 characters).

## 2) Start PostgreSQL and Redis
Infrastructure-only startup (plus app containers if defined in compose):

```bash
docker compose up -d postgres redis
```

## 3) Run Backend Locally
```bash
cd apps/backend
mvn spring-boot:run
```

Backend default URL:
- [http://localhost:8080](http://localhost:8080)

Health endpoint:
- [http://localhost:8080/api/health](http://localhost:8080/api/health)

## 4) Run Frontend Locally
```bash
cd apps/frontend
npm install
npm run dev
```

Frontend default URL:
- [http://localhost:5173](http://localhost:5173)

## 5) Run All Services with Docker Compose
From root:

```bash
docker compose up --build
```

Entry points:
- Nginx: [http://localhost:80](http://localhost:80)
- Backend direct: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Frontend direct: [http://localhost:5173](http://localhost:5173)

## 6) Verify Backend Health
```bash
curl http://localhost:8080/api/health
```

Expected JSON:

```json
{
  "success": true,
  "message": "Backend service is running",
  "data": {
    "status": "UP",
    "service": "gamified-study-hub-backend"
  },
  "timestamp": "2026-05-12T00:00:00Z"
}
```

## 7) Verify Frontend
Open [http://localhost:5173](http://localhost:5173) (or [http://localhost:80](http://localhost:80) via Nginx) and confirm the base shell shows:
- Project name
- "Base project is running"
- API base URL value

## Common Troubleshooting
- Port already in use:
  - Change the corresponding value in `.env` and restart services.
- Backend cannot connect to PostgreSQL:
  - Ensure `postgres` container is healthy (`docker compose ps`).
  - Recheck `POSTGRES_*` values in `.env`.
- Frontend cannot call backend:
  - Verify `VITE_API_BASE_URL` in `.env`.
  - Confirm backend health endpoint responds.
- Docker build fails on dependency install:
  - Retry after clearing local Docker cache or running `docker compose build --no-cache`.
