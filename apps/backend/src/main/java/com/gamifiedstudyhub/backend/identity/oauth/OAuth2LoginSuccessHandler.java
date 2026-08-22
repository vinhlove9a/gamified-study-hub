package com.gamifiedstudyhub.backend.identity.oauth;

import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.security.AuthCookieFactory;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.identity.FederatedIdentityService;
import com.gamifiedstudyhub.backend.mfa.MfaChallengeService;
import com.gamifiedstudyhub.backend.mfa.MfaService;
import com.gamifiedstudyhub.backend.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    private final MfaService mfaService;
    private final MfaChallengeService mfaChallengeService;
    private final AuthCookieFactory cookieFactory;
    private final String frontendOrigin;

    public OAuth2LoginSuccessHandler(
            FederatedIdentityService federatedIdentityService,
            JwtService jwtService,
            AuthSessionService authSessionService,
            AuthAuditService auditService,
            MfaService mfaService,
            MfaChallengeService mfaChallengeService,
            AuthCookieFactory cookieFactory,
            @Value("${FRONTEND_ORIGIN:http://localhost:5173}") String frontendOrigin
    ) {
        this.federatedIdentityService = federatedIdentityService;
        this.jwtService = jwtService;
        this.authSessionService = authSessionService;
        this.auditService = auditService;
        this.mfaService = mfaService;
        this.mfaChallengeService = mfaChallengeService;
        this.cookieFactory = cookieFactory;
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
        String targetOrigin = resolveTargetOrigin(request);

        // Second factor required? Do NOT issue a session yet. Carry the challenge in a
        // short-lived httpOnly cookie (kept out of the URL) and hand off to the SPA's MFA page.
        if (mfaService.isEnabled(user.getId())) {
            String challenge = mfaChallengeService.issue(user.getId());
            response.addHeader(HttpHeaders.SET_COOKIE,
                    cookieFactory.mfaChallengeCookie(challenge, Duration.ofMinutes(5)).toString());
            response.sendRedirect(targetOrigin + "/auth/mfa?oauth=1");
            return;
        }

        String accessToken = jwtService.generateAccessToken(user);
        authSessionService.issueSession(response, user.getId(), accessToken);
        auditService.record(AuthEventType.OAUTH_LOGIN, user.getId(), RequestMetadata.from(request),
                "provider=" + PROVIDER);
        response.sendRedirect(targetOrigin + "/auth/callback");
    }

    /** Pick the matching SPA origin when FRONTEND_ORIGIN is a comma-separated allowlist. */
    private String resolveTargetOrigin(HttpServletRequest request) {
        if (!frontendOrigin.contains(",")) {
            return frontendOrigin;
        }
        String[] origins = frontendOrigin.split(",");
        String requestHost = request.getHeader("Host");
        if (requestHost != null) {
            for (String origin : origins) {
                String cleanOrigin = origin.replace("http://", "").replace("https://", "");
                if (cleanOrigin.equalsIgnoreCase(requestHost.trim())) {
                    return origin;
                }
            }
        }
        return origins[0]; // default fallback
    }
}
