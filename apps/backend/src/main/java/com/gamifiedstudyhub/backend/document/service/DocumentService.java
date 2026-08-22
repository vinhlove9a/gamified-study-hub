package com.gamifiedstudyhub.backend.document.service;

import com.gamifiedstudyhub.backend.authz.WorkspaceGuard;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.exception.ForbiddenException;
import com.gamifiedstudyhub.backend.document.dto.DocumentDetailResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.dto.TagResponse;
import com.gamifiedstudyhub.backend.document.entity.Document;
import com.gamifiedstudyhub.backend.document.entity.DocumentTagRelation;
import com.gamifiedstudyhub.backend.document.entity.Tag;
import com.gamifiedstudyhub.backend.document.mapper.DocumentMapper;
import com.gamifiedstudyhub.backend.document.repository.DocumentRepository;
import com.gamifiedstudyhub.backend.document.repository.DocumentTagRelationRepository;
import com.gamifiedstudyhub.backend.document.repository.TagRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTagRelationRepository documentTagRelationRepository;
    private final TagRepository tagRepository;
    private final DocumentMapper documentMapper;
    private final WorkspaceGuard workspaceGuard;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentTagRelationRepository documentTagRelationRepository,
            TagRepository tagRepository,
            DocumentMapper documentMapper,
            WorkspaceGuard workspaceGuard
    ) {
        this.documentRepository = documentRepository;
        this.documentTagRelationRepository = documentTagRelationRepository;
        this.tagRepository = tagRepository;
        this.documentMapper = documentMapper;
        this.workspaceGuard = workspaceGuard;
    }

    public List<DocumentSummaryResponse> listDocuments(UUID workspaceId, String keyword) {
        requireWorkspaceMembership(workspaceId);

        List<Document> documents;
        if (keyword == null || keyword.isBlank()) {
            documents = documentRepository.findByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(workspaceId);
        } else {
            documents = documentRepository.findByWorkspaceIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    workspaceId,
                    keyword.trim()
            );
        }

        return documents.stream()
                .map(documentMapper::toSummary)
                .toList();
    }

    public DocumentDetailResponse getDocument(UUID documentId) {
        Document document = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.DOCUMENT_NOT_FOUND,
                        "Document not found",
                        HttpStatus.NOT_FOUND
                ));

        requireWorkspaceMembership(document.getWorkspaceId());

        List<DocumentTagRelation> relations = documentTagRelationRepository.findByIdDocumentId(documentId);
        Set<UUID> tagIds = relations.stream()
                .map(relation -> relation.getId().getTagId())
                .collect(Collectors.toSet());

        List<TagResponse> tags = tagIds.isEmpty()
                ? List.of()
                : tagRepository.findByIdInAndDeletedAtIsNull(tagIds)
                        .stream()
                        .map(documentMapper::toTagResponse)
                        .toList();

        return documentMapper.toDetail(document, tags);
    }

    /**
     * Documents are workspace-scoped: only active members of the owning workspace
     * (or platform admins) may read them. This is the baseline access check that
     * closes cross-workspace access; finer per-document visibility (FREE/VIP/PRIVATE)
     * is layered on later once coin-based unlocking exists.
     */
    private void requireWorkspaceMembership(UUID workspaceId) {
        if (!workspaceGuard.isMember(workspaceId)) {
            throw new ForbiddenException("You do not have access to this workspace");
        }
    }
}
