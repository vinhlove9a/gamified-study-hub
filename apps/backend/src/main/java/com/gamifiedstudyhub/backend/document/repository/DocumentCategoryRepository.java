package com.gamifiedstudyhub.backend.document.repository;

import com.gamifiedstudyhub.backend.document.entity.DocumentCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, UUID> {

    Optional<DocumentCategory> findByIdAndDeletedAtIsNull(UUID id);

    List<DocumentCategory> findByWorkspaceIdAndDeletedAtIsNullOrderBySortOrderAscNameAsc(UUID workspaceId);
}
