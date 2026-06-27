package com.gamifiedstudyhub.backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Materializes the (deferred) CSRF token on every request so Spring's
 * CookieCsrfTokenRepository actually writes the readable XSRF-TOKEN cookie.
 * Without this, a fresh SPA has no token to echo back in X-XSRF-TOKEN.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Touch the token value to trigger the repository to persist the cookie.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
