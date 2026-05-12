package com.gamifiedstudyhub.backend.common.web;

import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX)
public class HealthCheckController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> health = Map.of(
                "status", "UP",
                "service", "gamified-study-hub-backend"
        );

        return ApiResponse.success("Backend service is running", health);
    }
}
