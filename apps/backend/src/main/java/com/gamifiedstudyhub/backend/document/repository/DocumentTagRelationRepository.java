package com.gamifiedstudyhub.backend.document.repository;

import com.gamifiedstudyhub.backend.document.entity.DocumentTagRelation;
import com.gamifiedstudyhub.backend.document.entity.DocumentTagRelationId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTagRelationRepository extends JpaRepository<DocumentTagRelation, DocumentTagRelationId> {

    List<DocumentTagRelation> findByIdDocumentId(UUID documentId);
}
