# Project Structure

## Monorepo Overview
This repository uses a monorepo layout to keep backend, frontend, shared contracts, infrastructure, and documentation versioned together.

## Why `apps/backend` and `apps/frontend`
- `apps/backend`: owns Spring Boot API service code and backend runtime configuration.
- `apps/frontend`: owns Vue client code, UI build tooling, and frontend runtime packaging.

This separation keeps deployable applications isolated while still allowing shared governance and tooling.

## Why `packages/shared-types` Exists
`packages/shared-types` is reserved for reusable TypeScript contracts shared across client/server boundaries.

At this stage, only generic API response wrappers are included.

## Why `infra` Exists
`infra` centralizes deployment/development infrastructure assets:
- `infra/docker`: docker-related helper assets (reserved for future use)
- `infra/nginx`: reverse-proxy configuration
- `infra/scripts`: repeatable developer automation scripts

## Why Docs Are Split
- `docs/architecture`: system structure and technical architecture references
- `docs/development`: local setup and developer workflow guides
- `docs/decisions`: Architecture Decision Records (ADRs)

This improves long-term maintainability by separating conceptual architecture, operational steps, and rationale history.

## Placement Rules
Do place:
- Application source code under `apps/*/src`
- Reusable package code under `packages/*`
- Infrastructure config/scripts under `infra/*`
- Project guidance under `docs/*`

Do not place:
- Business/domain code in `infra` or `docs`
- Environment-specific secrets in committed files
- Backend runtime files in frontend folders (and vice versa)
