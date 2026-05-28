package com.gamifiedstudyhub.backend.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        UUID workspaceId,
        UUID categoryId,
        String categoryName,
        String title,
        String slug,
        String description,
        String fileType,
        Long fileSize,
        String visibility,
        String status,
        int coinPrice,
        long downloadCount,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
