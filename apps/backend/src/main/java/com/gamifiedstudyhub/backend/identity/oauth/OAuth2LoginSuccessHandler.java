package com.gamifiedstudyhub.backend.identity.oauth;

import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.identity.FederatedIdentityService;
import com.gamifiedstudyhub.backend.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** On successful Google login: find-or-link the local user, issue our cookies, redirect to the SPA. */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String PROVIDER = "google";

    private final FederatedIdentityService federatedIdentityService;
    private final JwtService jwtService;
    private final AuthSessionService authSessionService;
    private final AuthAuditService auditService;
    private final String frontendOrigin;

    public OAuth2LoginSuccessHandler(
            FederatedIdentityService federatedIdentityService,
            JwtService jwtService,
            AuthSessionService authSessionService,
            AuthAuditService auditService,
            @Value("${FRONTEND_ORIGIN:http://localhost:5173}") String frontendOrigin
    ) {
        this.federatedIdentityService = federatedIdentityService;
        this.jwtService = jwtService;
        this.authSessionService = authSessionService;
        this.auditService = auditService;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String sub = oAuth2User.getName(); // OIDC "sub"
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        boolean emailVerified = Boolean.TRUE.equals(oAuth2User.getAttribute("email_verified"))
                || "true".equalsIgnoreCase(String.valueOf(oAuth2User.getAttribute("email_verified")));

        User user = federatedIdentityService.resolve(PROVIDER, sub, email, emailVerified, name);

        String accessToken = jwtService.generateAccessToken(user);
        authSessionService.issueSession(response, user.getId(), accessToken);
        auditService.record(AuthEventType.OAUTH_LOGIN, user.getId(), RequestMetadata.from(request),
                "provider=" + PROVIDER);

        response.sendRedirect(frontendOrigin + "/auth/callback");
    }
}
