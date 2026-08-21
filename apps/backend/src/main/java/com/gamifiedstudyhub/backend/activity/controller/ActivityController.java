package com.gamifiedstudyhub.backend.activity.controller;

import com.gamifiedstudyhub.backend.activity.dto.ActivityRequest;
import com.gamifiedstudyhub.backend.activity.dto.ActivityResponse;
import com.gamifiedstudyhub.backend.activity.service.ActivityService;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reference endpoints demonstrating the MongoDB + Kafka + Elasticsearch pipeline.
 * Registered only when {@code app.features.activity.enabled=true}. Secured like the
 * rest of {@code /api/**} (authenticated user required).
 */
@Validated
@RestController
@ConditionalOnProperty(prefix = "app.features.activity", name = "enabled", havingValue = "true")
@RequestMapping(AppConstants.API_V1_PREFIX + "/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    @Operation(summary = "Record a study activity (writes MongoDB, publishes to Kafka)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<ActivityResponse> record(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid ActivityRequest request
    ) {
        ActivityResponse response = activityService.record(principal.getUserId(), request);
        return ApiResponse.success("Activity recorded", response);
    }

    @GetMapping
    @Operation(summary = "List my recent activities (from MongoDB)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<List<ActivityResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success("Activities retrieved", activityService.list(principal.getUserId()));
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search activities (from Elasticsearch)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<List<ActivityResponse>> search(@RequestParam("q") @NotBlank String query) {
        return ApiResponse.success("Search results", activityService.search(query));
    }
}
