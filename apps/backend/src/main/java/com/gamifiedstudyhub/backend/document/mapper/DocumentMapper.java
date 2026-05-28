package com.gamifiedstudyhub.backend.document.mapper;

import com.gamifiedstudyhub.backend.document.dto.DocumentCategoryResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentDetailResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.dto.TagResponse;
import com.gamifiedstudyhub.backend.document.entity.Document;
import com.gamifiedstudyhub.backend.document.entity.DocumentCategory;
import com.gamifiedstudyhub.backend.document.entity.Tag;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentSummaryResponse toSummary(Document document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getWorkspaceId(),
                document.getCategory() != null ? document.getCategory().getId() : null,
                document.getCategory() != null ? document.getCategory().getName() : null,
                document.getTitle(),
                document.getSlug(),
                document.getDescription(),
                document.getFileType(),
                document.getFileSize(),
                document.getVisibility() != null ? document.getVisibility().name() : null,
                document.getStatus() != null ? document.getStatus().name() : null,
                document.getCoinPrice(),
                document.getDownloadCount(),
                document.getPublishedAt(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public DocumentDetailResponse toDetail(Document document, List<TagResponse> tags) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getWorkspaceId(),
                toCategoryResponse(document.getCategory()),
                document.getTitle(),
                document.getSlug(),
                document.getDescription(),
                document.getOriginalFileName(),
                document.getFileType(),
                document.getFileSize(),
                document.getVisibility() != null ? document.getVisibility().name() : null,
                document.getStatus() != null ? document.getStatus().name() : null,
                document.getCoinPrice(),
                document.getDownloadCount(),
                document.getUploadedBy() != null ? document.getUploadedBy().getId() : null,
                document.getPublishedAt(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                tags
        );
    }

    public DocumentCategoryResponse toCategoryResponse(DocumentCategory category) {
        if (category == null) {
            return null;
        }
        return new DocumentCategoryResponse(
                category.getId(),
                category.getWorkspaceId(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder()
        );
    }

    public TagResponse toTagResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getWorkspaceId(),
                tag.getName(),
                tag.getSlug()
        );
    }
}
