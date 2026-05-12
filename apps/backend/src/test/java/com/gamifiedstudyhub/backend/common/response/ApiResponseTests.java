package com.gamifiedstudyhub.backend.common.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiResponseTests {

    @Test
    void success_withData_shouldBuildSuccessResponse() {
        ApiResponse<Map<String, String>> response = ApiResponse.success(Map.of("status", "UP"));

        assertTrue(response.success());
        assertEquals("Request successful", response.message());
        assertEquals("UP", response.data().get("status"));
        assertNotNull(response.timestamp());
    }

    @Test
    void success_withMessageAndData_shouldBuildSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Created", "value");

        assertTrue(response.success());
        assertEquals("Created", response.message());
        assertEquals("value", response.data());
        assertNotNull(response.timestamp());
    }

    @Test
    void error_shouldBuildErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Failure");

        assertFalse(response.success());
        assertEquals("Failure", response.message());
        assertNull(response.data());
        assertNotNull(response.timestamp());
    }
}
