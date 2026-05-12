# Database Foundation

## 1) Purpose
This document defines the Step 3 database foundation for Gamified Study Hub MVP.

Scope of this step:
- Core relational schema for users, workspaces, documents, coins, missions, and activity logs
- Flyway migration design and deterministic DDL
- Constraints and indexes for data integrity and performance
- Multi-tenant-ready table strategy

## 2) Multi-Tenant Strategy
- Workspace is the tenant boundary.
- Workspace-scoped tables include `workspace_id` (including join/ledger tables such as `document_tag_relations` and `coin_transactions`).
- Queries in future modules must always filter by `workspace_id`.
- Cross-tenant isolation is enforced by application query boundaries and workspace foreign keys.

## 3) Table Overview
Core tables in `V2__core_schema.sql`:
- `users`
- `workspaces`
- `workspace_members`
- `workspace_settings`
- `document_categories`
- `documents`
- `document_tags`
- `document_tag_relations`
- `coin_wallets`
- `coin_transactions`
- `missions`
- `mission_completions`
- `document_unlocks`
- `download_logs`
- `daily_checkins`

## 4) Relationship Overview
- Users ↔ Workspaces: many-to-many via `workspace_members`
- Workspace → Documents/Categories/Tags/Missions/Wallets/Logs: one-to-many
- Documents ↔ Tags: many-to-many via `document_tag_relations`
- Wallet → Coin Transactions: one-to-many immutable ledger
- Mission → Mission Completions: one-to-many
- Document → Document Unlocks/Download Logs: one-to-many

## 5) Soft Delete Strategy
Soft delete is enabled where lifecycle recovery/audit is useful (`deleted_at`):
- `users`, `workspaces`, `workspace_members`
- `document_categories`, `documents`, `document_tags`
- `missions`

Partial unique indexes use `WHERE deleted_at IS NULL` for active-record uniqueness.

## 6) UUID Strategy
- All primary keys use `UUID`.
- IDs are designed for application-side generation (compatible with existing `BaseEntity` UUID strategy).
- No PostgreSQL enum ID generation logic is used.

## 7) Timestamp/Audit Strategy
- Mutable tables include:
  - `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
  - `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- Immutable/event tables avoid `updated_at`:
  - `coin_transactions`
  - `mission_completions`
  - `document_unlocks`
  - `download_logs`
  - `daily_checkins`

## 8) Indexing Strategy
Indexes are added for:
- Tenant filtering: `workspace_id`
- Join paths: foreign key columns
- Lifecycle/status filtering: `status`, `visibility`, `created_at`, `published_at`
- Ledger timelines: transaction/completion/download timestamps
- Search support: GIN full-text index on `documents.title`
- Composite query patterns such as `(workspace_id, status)` and `(workspace_id, user_id)`

## 9) Constraint Strategy
- Business-safe checks:
  - non-negative balances, prices, sizes, counters, rewards
  - valid string-state values via `CHECK (... IN (...))`
  - mission time window validity (`end_at >= start_at` when both present)
- Uniqueness:
  - case-insensitive active email uniqueness (`lower(email)` with soft-delete partial index)
  - active slug uniqueness for workspace-scoped resources
  - one wallet per workspace user
  - one unlock per user per document
  - one daily check-in per workspace/user/date

## 10) Coin Ledger Strategy
- `coin_wallets` stores current aggregates (`balance`, `total_earned`, `total_spent`).
- `coin_transactions` is append-only and immutable.
- Transaction rows track `amount`, `balance_before`, and `balance_after` for auditability.

## 11) Document Access Strategy
- `documents.visibility` supports `FREE`, `VIP`, `PRIVATE`.
- `documents.coin_price` supports unlock cost metadata.
- `document_unlocks` records purchased/unlocked access events.
- `download_logs` records download activity for analytics and abuse monitoring.

## 12) Intentionally Not Implemented Yet
- Authentication/JWT and security policy enforcement
- Authorization rules and role-policy engine
- Business services/controllers/repositories for domain workflows
- File upload storage integration and signed URL flows
- Payment/subscription schema
- Seed data
