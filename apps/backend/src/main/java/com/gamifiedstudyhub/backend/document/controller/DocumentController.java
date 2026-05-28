package com.gamifiedstudyhub.backend.document.controller;

import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentDetailResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(AppConstants.API_V1_PREFIX + "/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "List workspace documents metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<List<DocumentSummaryResponse>> listDocuments(
            @RequestParam("workspaceId") @NotNull UUID workspaceId,
            @RequestParam(value = "q", required = false) String keyword
    ) {
        List<DocumentSummaryResponse> response = documentService.listDocuments(workspaceId, keyword);
        return ApiResponse.success("Documents retrieved", response);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document metadata details", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<DocumentDetailResponse> getDocument(@PathVariable UUID documentId) {
        DocumentDetailResponse response = documentService.getDocument(documentId);
        return ApiResponse.success("Document retrieved", response);
    }
}
