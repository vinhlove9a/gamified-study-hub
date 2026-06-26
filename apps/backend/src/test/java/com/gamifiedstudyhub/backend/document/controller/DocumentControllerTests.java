package com.gamifiedstudyhub.backend.document.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetailsService;
import com.gamifiedstudyhub.backend.auth.security.JwtAuthenticationEntryPoint;
import com.gamifiedstudyhub.backend.auth.security.JwtAuthenticationFilter;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.security.RestAccessDeniedHandler;
import com.gamifiedstudyhub.backend.common.config.SecurityConfig;
import com.gamifiedstudyhub.backend.document.dto.DocumentDetailResponse;
import com.gamifiedstudyhub.backend.document.dto.DocumentSummaryResponse;
import com.gamifiedstudyhub.backend.document.service.DocumentService;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(controllers = DocumentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class})
class DocumentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String ACCESS_TOKEN = "valid-access-token";

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail("doc.user@example.com");
        user.setFullName("Doc User");
        user.setStatus(UserStatus.ACTIVE);

        when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(userId);
        when(jwtService.isAccessTokenValid(ACCESS_TOKEN)).thenReturn(true);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(new CustomUserDetails(user));
    }

    @Test
    void listDocuments_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/documents").queryParam("workspaceId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).listDocuments(any(UUID.class), any());
    }

    @Test
    void listDocuments_shouldReturnSuccessWhenAuthenticated() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        DocumentSummaryResponse summary = new DocumentSummaryResponse(
                UUID.randomUUID(),
                workspaceId,
                null,
                null,
                "Doc",
                "doc",
                "desc",
                null,
                null,
                "FREE",
                "DRAFT",
                0,
                0,
                null,
                null,
                null
        );

        when(documentService.listDocuments(workspaceId, null)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/documents")
                        .header(AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .queryParam("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Doc"));

        verify(documentService).listDocuments(workspaceId, null);
    }

    @Test
    void getDocument_shouldReturnSuccessWhenAuthenticated() throws Exception {
        UUID documentId = UUID.randomUUID();
        DocumentDetailResponse detail = new DocumentDetailResponse(
                documentId,
                UUID.randomUUID(),
                null,
                "Doc",
                "doc",
                "desc",
                null,
                null,
                null,
                "FREE",
                "DRAFT",
                0,
                0,
                null,
                null,
                null,
                null,
                List.of()
        );

        when(documentService.getDocument(documentId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/documents/{documentId}", documentId)
                        .header(AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(documentId.toString()));

        verify(documentService).getDocument(documentId);
    }
}
