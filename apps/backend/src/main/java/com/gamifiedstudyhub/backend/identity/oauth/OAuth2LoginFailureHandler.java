package com.gamifiedstudyhub.backend.identity.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/** On OAuth login failure, redirect back to the SPA error page. */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String frontendOrigin;

    public OAuth2LoginFailureHandler(
            @Value("${FRONTEND_ORIGIN:http://localhost:5173}") String frontendOrigin
    ) {
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String reason = URLEncoder.encode("oauth_failed", StandardCharsets.UTF_8);
        response.sendRedirect(frontendOrigin + "/auth/error?reason=" + reason);
    }
}
