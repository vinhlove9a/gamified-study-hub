# Gamified Study Hub

## Project Purpose
Gamified Study Hub is a web-based SaaS platform for learning communities, tutors, university clubs, and small education centers.

The long-term platform direction includes workspace-based learning management, document delivery, and gamified engagement mechanics.

## Current Status
**Base project foundation**

This repository currently provides the monorepo structure, backend/frontend base setup, infrastructure configuration, and development conventions only.

Not implemented yet:
- Authentication and authorization
- Business/domain modules
- Database schema and business migrations
- Document/coin/mission/gamification logic
- Multi-tenant domain behavior

## Tech Stack
- Backend: Java 21, Spring Boot 3.x, Maven, PostgreSQL, Flyway
- Frontend: Vue 3, Vite, TypeScript, TailwindCSS
- Infrastructure: Docker, Docker Compose, Nginx
- Monorepo: apps + packages + infra + docs

## Monorepo Structure
```text
gamified-study-hub/
├── apps/
│   ├── backend/
│   └── frontend/
├── packages/
│   └── shared-types/
├── infra/
│   ├── docker/
│   ├── nginx/
│   └── scripts/
├── docs/
│   ├── architecture/
│   ├── development/
│   └── decisions/
├── .github/
│   └── workflows/
├── docker-compose.yml
├── docker-compose.override.yml
├── .env.example
├── .gitignore
├── .editorconfig
├── README.md
└── Makefile
```

## Local Development Overview
1. Copy environment template:
   ```bash
   cp .env.example .env
   ```
2. Start infrastructure and app containers:
   ```bash
   make dev-up
   ```
3. Or run backend/frontend locally without Docker (see sections below).

## Start Infrastructure Only
For PostgreSQL + Redis + app stack via Docker Compose:
```bash
make dev-up
```

Stop:
```bash
make dev-down
```

Logs:
```bash
make dev-logs
```

## Run Backend Locally
```bash
cd apps/backend
mvn spring-boot:run
```

Health check:
```bash
curl http://localhost:8080/api/health
```

## Run Frontend Locally
```bash
cd apps/frontend
npm install
npm run dev
```

Frontend URL:
- [http://localhost:5173](http://localhost:5173)

## Run Full Stack with Docker Compose
```bash
docker compose up --build
```

Entry points:
- Nginx: [http://localhost:80](http://localhost:80)
- Backend direct: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Frontend direct: [http://localhost:5173](http://localhost:5173)

## Environment Variables
Environment defaults are documented in `.env.example`.

Key groups:
- App: `APP_ENV`, `APP_NAME`
- Backend: `BACKEND_PORT`, `SPRING_PROFILES_ACTIVE`, `FRONTEND_ORIGIN`
- Frontend: `FRONTEND_PORT`, `VITE_API_BASE_URL`
- PostgreSQL: `POSTGRES_*`
- Redis: `REDIS_*`
- Nginx: `NGINX_PORT`

## Development Principles
- Build clean foundations first; avoid premature feature implementation.
- Keep boundaries explicit between backend, frontend, shared packages, and infrastructure.
- Use environment-based configuration for local and production profiles.
- Keep documentation and decisions versioned in the repository.

## Future Modules (Planned, Not Implemented)
- Identity and access management (auth/authz)
- Workspace and tenant model
- Document management and unlocking
- Coin/reward engine
- Mission and progression workflows
- Tutor/community management features
