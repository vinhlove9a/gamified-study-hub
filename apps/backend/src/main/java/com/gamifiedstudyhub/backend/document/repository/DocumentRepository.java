package com.gamifiedstudyhub.backend.document.repository;

import com.gamifiedstudyhub.backend.document.entity.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndDeletedAtIsNull(UUID id);

    List<Document> findByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID workspaceId);

    List<Document> findByWorkspaceIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            UUID workspaceId,
            String title
    );
}
