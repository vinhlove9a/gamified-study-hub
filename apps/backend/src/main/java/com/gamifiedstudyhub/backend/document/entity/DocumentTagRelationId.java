package com.gamifiedstudyhub.backend.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DocumentTagRelationId implements Serializable {

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    public DocumentTagRelationId() {
    }

    public DocumentTagRelationId(UUID documentId, UUID tagId) {
        this.documentId = documentId;
        this.tagId = tagId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getTagId() {
        return tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentTagRelationId that)) {
            return false;
        }
        return Objects.equals(documentId, that.documentId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, tagId);
    }
}
