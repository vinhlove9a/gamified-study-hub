package com.gamifiedstudyhub.backend.common.response;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean first,
        boolean last
) {
    public PageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
