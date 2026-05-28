# Document Domain Foundation (Step 10A)

## Scope
Step 10A introduces backend document-domain foundation structure only. It aligns Java entities and read-oriented APIs with existing `V2__core_schema.sql` tables.

Implemented in this step:
- Document domain package skeleton (`controller`, `dto`, `entity`, `mapper`, `repository`, `service`)
- Entity mappings for existing V2 document tables
- Read-only metadata endpoints for documents
- Mapper/service/controller unit tests for foundation behavior

## V2 Tables Mapped
Mapped directly from existing migration `apps/backend/src/main/resources/db/migration/V2__core_schema.sql`:
- `documents` -> `Document`
- `document_categories` -> `DocumentCategory`
- `document_tags` -> `Tag`
- `document_tag_relations` -> `DocumentTagRelation`

Not mapped in Step 10A:
- `document_unlocks`
- `download_logs`

Reason: unlock/download business workflows are explicitly deferred.

## Entities and Repositories
Entities:
- `Document`
- `DocumentCategory`
- `Tag`
- `DocumentTagRelation` (+ `DocumentTagRelationId` composite key)
- supporting enums: `DocumentStatus`, `DocumentVisibility`

Repositories:
- `DocumentRepository`
- `DocumentCategoryRepository`
- `TagRepository`
- `DocumentTagRelationRepository`

Repository methods remain minimal for foundation-level access:
- active (non-deleted) find by id
- list active documents by workspace
- title keyword metadata search via SQL `LIKE` style JPA method

## DTO and Mapper Foundation
DTOs:
- `DocumentSummaryResponse`
- `DocumentDetailResponse`
- `DocumentCategoryResponse`
- `TagResponse`
- `CreateDocumentRequest` (metadata-only placeholder)
- `UpdateDocumentMetadataRequest` (metadata-only placeholder)

Mapper:
- `DocumentMapper` maps entities -> response DTOs only.
- No business logic is embedded in mapper.

## Service Foundation
`DocumentService` provides safe read methods:
- `listDocuments(UUID workspaceId, String keyword)`
- `getDocument(UUID documentId)`

Behavior:
- read from non-deleted document records
- throw `BusinessException(DOCUMENT_NOT_FOUND)` for missing document
- resolve tag metadata through relation table

## Controller Foundation
Added authenticated metadata endpoints only:
- `GET /api/v1/documents?workspaceId=<uuid>&q=<optional keyword>`
- `GET /api/v1/documents/{documentId}`

These endpoints are protected by existing `SecurityConfig` rule (`/api/**` authenticated). No public document endpoint was added.

## Deferred Intentionally
The following remain out of scope in Step 10A:
- file upload endpoint and multipart handling
- storage provider integration (S3/local disk)
- signed download URL / download endpoint
- Elasticsearch search integration
- coin unlock spending logic
- mission reward/task completion logic
- workspace authorization hardening beyond current auth guardrails
- frontend document module/UI integration

## Validation Summary
Step 10A validation includes:
- backend package (`mvn -DskipTests package`)
- backend tests (`mvn test`) with environment note if Mockito/Java tooling issue appears
- frontend sanity (`typecheck`, `build`, `lint`)
- `docker compose config` and backend startup log check for Flyway/migration continuity

## Known Limitations
- Create/update metadata DTOs are foundation placeholders and not exposed as write endpoints yet.
- Workspace-level authorization rules for document access are deferred to a follow-up hardening step.
