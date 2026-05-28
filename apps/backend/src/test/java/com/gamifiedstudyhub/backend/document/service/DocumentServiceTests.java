package com.gamifiedstudyhub.backend.document.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.entity.Document;
import com.gamifiedstudyhub.backend.document.mapper.DocumentMapper;
import com.gamifiedstudyhub.backend.document.repository.DocumentRepository;
import com.gamifiedstudyhub.backend.document.repository.DocumentTagRelationRepository;
import com.gamifiedstudyhub.backend.document.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTagRelationRepository documentTagRelationRepository;

    @Mock
    private TagRepository tagRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
                documentRepository,
                documentTagRelationRepository,
                tagRepository,
                new DocumentMapper()
        );
    }

    @Test
    void listDocuments_shouldReturnMappedDocuments() {
        UUID workspaceId = UUID.randomUUID();

        Document document = new Document();
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        document.setWorkspaceId(workspaceId);
        document.setTitle("Document A");
        document.setSlug("document-a");

        when(documentRepository.findByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(document));

        List<DocumentSummaryResponse> response = documentService.listDocuments(workspaceId, null);

        assertEquals(1, response.size());
        assertEquals("Document A", response.get(0).title());
        verify(documentRepository).findByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(workspaceId);
    }

    @Test
    void getDocument_shouldThrowBusinessExceptionWhenDocumentDoesNotExist() {
        UUID documentId = UUID.randomUUID();
        when(documentRepository.findByIdAndDeletedAtIsNull(documentId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.getDocument(documentId));

        assertEquals(ErrorCodes.DOCUMENT_NOT_FOUND, exception.getCode());
    }
}
