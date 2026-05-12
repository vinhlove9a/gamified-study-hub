package com.gamifiedstudyhub.backend.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthCheckControllerTests {

    @Test
    void healthEndpoint_shouldReturnExpectedResponseShape() {
        HealthCheckController controller = new HealthCheckController();
        ApiResponse<Map<String, String>> response = controller.health();

        assertTrue(response.success());
        assertEquals("Backend service is running", response.message());
        assertEquals("UP", response.data().get("status"));
        assertEquals("gamified-study-hub-backend", response.data().get("service"));
        assertNotNull(response.timestamp());
    }
}
