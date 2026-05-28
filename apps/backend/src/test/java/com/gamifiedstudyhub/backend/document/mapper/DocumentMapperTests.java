package com.gamifiedstudyhub.backend.document.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gamifiedstudyhub.backend.document.dto.DocumentDetailResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.dto.TagResponse;
import com.gamifiedstudyhub.backend.document.entity.Document;
import com.gamifiedstudyhub.backend.document.entity.DocumentCategory;
import com.gamifiedstudyhub.backend.document.entity.DocumentStatus;
import com.gamifiedstudyhub.backend.document.entity.DocumentVisibility;
import com.gamifiedstudyhub.backend.document.entity.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DocumentMapperTests {

    private final DocumentMapper mapper = new DocumentMapper();

    @Test
    void toSummary_shouldMapMetadataWithoutStorageInternals() {
        UUID documentId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        DocumentCategory category = new DocumentCategory();
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        category.setWorkspaceId(workspaceId);
        category.setName("Guides");

        Document document = new Document();
        ReflectionTestUtils.setField(document, "id", documentId);
        ReflectionTestUtils.setField(document, "createdAt", Instant.now());
        ReflectionTestUtils.setField(document, "updatedAt", Instant.now());
        document.setWorkspaceId(workspaceId);
        document.setCategory(category);
        document.setTitle("Study Plan");
        document.setSlug("study-plan");
        document.setDescription("A starter document");
        document.setFileType("application/pdf");
        document.setFileSize(1024L);
        document.setVisibility(DocumentVisibility.FREE);
        document.setStatus(DocumentStatus.PUBLISHED);
        document.setCoinPrice(0);
        document.setDownloadCount(10L);

        DocumentSummaryResponse response = mapper.toSummary(document);

        assertEquals(documentId, response.id());
        assertEquals("Study Plan", response.title());
        assertEquals("Guides", response.categoryName());
        assertEquals("FREE", response.visibility());
        assertEquals("PUBLISHED", response.status());
    }

    @Test
    void toDetail_shouldMapCategoryAndTags() {
        UUID documentId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        DocumentCategory category = new DocumentCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);
        category.setWorkspaceId(workspaceId);
        category.setName("Docs");
        category.setSlug("docs");
        category.setSortOrder(1);

        Document document = new Document();
        ReflectionTestUtils.setField(document, "id", documentId);
        ReflectionTestUtils.setField(document, "createdAt", Instant.now());
        ReflectionTestUtils.setField(document, "updatedAt", Instant.now());
        document.setWorkspaceId(workspaceId);
        document.setCategory(category);
        document.setTitle("Doc title");
        document.setSlug("doc-title");
        document.setOriginalFileName("doc.pdf");
        document.setVisibility(DocumentVisibility.VIP);
        document.setStatus(DocumentStatus.DRAFT);

        Tag tag = new Tag();
        ReflectionTestUtils.setField(tag, "id", tagId);
        tag.setWorkspaceId(workspaceId);
        tag.setName("math");
        tag.setSlug("math");

        TagResponse tagResponse = mapper.toTagResponse(tag);
        DocumentDetailResponse response = mapper.toDetail(document, List.of(tagResponse));

        assertNotNull(response.category());
        assertEquals(categoryId, response.category().id());
        assertEquals("VIP", response.visibility());
        assertEquals("DRAFT", response.status());
        assertFalse(response.tags().isEmpty());
        assertEquals(tagId, response.tags().get(0).id());
    }
}
