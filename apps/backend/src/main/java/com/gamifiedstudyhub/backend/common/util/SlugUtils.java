package com.gamifiedstudyhub.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private static final String DEFAULT_SLUG = "item";

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return DEFAULT_SLUG;
        }

        String normalized = Normalizer.normalize(input.replace('Đ', 'D').replace('đ', 'd'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return normalized.isBlank() ? DEFAULT_SLUG : normalized;
    }
}
