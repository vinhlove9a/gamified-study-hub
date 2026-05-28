package com.gamifiedstudyhub.backend.document.dto;

import java.util.UUID;

public record DocumentCategoryResponse(
        UUID id,
        UUID workspaceId,
        UUID parentId,
        String name,
        String slug,
        String description,
        int sortOrder
) {
}
