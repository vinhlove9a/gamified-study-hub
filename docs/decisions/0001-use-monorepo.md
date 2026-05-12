# 0001 - Use Monorepo for Gamified Study Hub

## Status
Accepted

## Context
Gamified Study Hub requires coordinated evolution between backend APIs, frontend clients, shared data contracts, infrastructure setup, and documentation.

A fragmented repository approach would increase synchronization overhead and complicate cross-cutting changes (for example API contract updates affecting both services).

## Decision
Adopt a monorepo structure with clear top-level boundaries:
- `apps/backend`
- `apps/frontend`
- `packages/shared-types`
- `infra/*`
- `docs/*`

Keep the initial phase focused on foundational setup only, postponing business modules until base tooling and conventions are stable.

## Consequences
Positive:
- Single source of truth for system evolution
- Easier alignment between API and frontend contracts
- Shared developer workflows and scripts
- Simplified onboarding and environment setup

Trade-offs:
- Requires discipline in folder boundaries and ownership
- Build/tooling complexity can grow over time if governance is weak

Mitigation:
- Enforce explicit project structure and documented conventions from the start
- Track architectural decisions in ADRs
