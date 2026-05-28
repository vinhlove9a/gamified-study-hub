package com.gamifiedstudyhub.backend.document.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentDetailResponse(
        UUID id,
        UUID workspaceId,
        DocumentCategoryResponse category,
        String title,
        String slug,
        String description,
        String originalFileName,
        String fileType,
        Long fileSize,
        String visibility,
        String status,
        int coinPrice,
        long downloadCount,
        UUID uploadedBy,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<TagResponse> tags
) {
}
