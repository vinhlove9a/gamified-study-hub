package com.gamifiedstudyhub.backend.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlugUtilsTests {

    @Test
    void toSlug_shouldNormalizeAndReplaceInvalidCharacters() {
        String slug = SlugUtils.toSlug("  Cà phê Sữa Đá @ 2026!  ");
        assertEquals("ca-phe-sua-da-2026", slug);
    }

    @Test
    void toSlug_shouldReturnDefaultForNullInput() {
        assertEquals("item", SlugUtils.toSlug(null));
    }

    @Test
    void toSlug_shouldReturnDefaultWhenCleanedValueIsBlank() {
        assertEquals("item", SlugUtils.toSlug("@@@"));
    }
}
