package com.gamifiedstudyhub.backend.auth.security;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final CookieProperties cookieProperties;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            CookieProperties cookieProperties
    ) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            if (!jwtService.isAccessTokenValid(token)) {
                setAuthError(request, ErrorCodes.INVALID_TOKEN, "Invalid token");
                filterChain.doFilter(request, response);
                return;
            }

            CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);
            if (!userDetails.isEnabled()) {
                setAuthError(request, ErrorCodes.UNAUTHORIZED, "Unauthorized");
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            setAuthError(request, ErrorCodes.TOKEN_EXPIRED, "Token has expired");
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            setAuthError(request, ErrorCodes.INVALID_TOKEN, "Invalid token");
        }

        filterChain.doFilter(request, response);
    }

    /** Access token from the httpOnly cookie first, then the Authorization Bearer header (API clients/tests). */
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieProperties.getAccessName().equals(cookie.getName())
                        && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue().trim();
                }
            }
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private void setAuthError(HttpServletRequest request, String code, String message) {
        request.setAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, code);
        request.setAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_MESSAGE_ATTRIBUTE, message);
    }
}
