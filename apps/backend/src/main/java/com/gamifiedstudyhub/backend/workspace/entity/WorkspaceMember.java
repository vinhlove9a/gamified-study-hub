package com.gamifiedstudyhub.backend.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Workspace-scoped membership + role (OWNER/ADMIN/TEACHER/STUDENT). Read-only
 * here — used by {@code WorkspaceGuard} for workspace-scoped authorization.
 * IDs are mapped as plain UUID columns (no JPA relations) to mirror the
 * existing {@code DocumentTagRelation} convention.
 */
@Entity
@Table(name = "workspace_members")
public class WorkspaceMember {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
