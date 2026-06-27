package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.auth.mapper.AuthMapper;
import com.gamifiedstudyhub.backend.auth.security.AuthCookieFactory;
import com.gamifiedstudyhub.backend.auth.security.CookieProperties;
import com.gamifiedstudyhub.backend.auth.security.JwtProperties;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.token.RefreshTokenException;
import com.gamifiedstudyhub.backend.auth.token.RefreshTokenService;
import com.gamifiedstudyhub.backend.authz.service.AuthorityService;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues/refreshes/clears the httpOnly auth cookies (access JWT + rotating
 * refresh token). The token transport layer — kept out of {@link AuthService}
 * so credential logic stays free of the servlet API.
 */
@Service
public class AuthSessionService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieFactory cookieFactory;
    private final CookieProperties cookieProperties;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final AuthorityService authorityService;
    private final AuthAuditService auditService;

    public AuthSessionService(
            JwtService jwtService,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService,
            AuthCookieFactory cookieFactory,
            CookieProperties cookieProperties,
            UserRepository userRepository,
            AuthMapper authMapper,
            AuthorityService authorityService,
            AuthAuditService auditService
    ) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
        this.cookieProperties = cookieProperties;
        this.userRepository = userRepository;
        this.authMapper = authMapper;
        this.authorityService = authorityService;
        this.auditService = auditService;
    }

    /** Mint access + refresh cookies for a freshly authenticated user (login/register). */
    public void issueSession(HttpServletResponse response, UUID userId, String accessToken) {
        Duration accessMaxAge = Duration.ofMinutes(jwtProperties.getAccessTokenExpirationMinutes());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieFactory.accessCookie(accessToken, accessMaxAge).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieFactory.refreshCookie(refreshTokenService.issue(userId), refreshTokenService.getTtl()).toString());
    }

    /** Rotate the refresh cookie and mint a fresh access cookie. Clears cookies on failure. */
    @Transactional(readOnly = true)
    public UserSummaryResponse refresh(HttpServletRequest request, HttpServletResponse response, RequestMetadata meta) {
        String refreshValue = readCookie(request, cookieProperties.getRefreshName());
        try {
            RefreshTokenService.Rotation rotation = refreshTokenService.rotate(refreshValue);
            User user = userRepository.findByIdAndDeletedAtIsNull(rotation.userId())
                    .filter(u -> !UserStatus.DISABLED.equals(u.getStatus()))
                    .orElseThrow(() -> new RefreshTokenException("Account is no longer active", false));

            String accessToken = jwtService.generateAccessToken(user);
            issueRotated(response, accessToken, rotation.cookieValue());

            auditService.record(AuthEventType.TOKEN_REFRESH, user.getId(), meta);
            List<String> authorities = authorityService.resolveAuthorityCodes(user.getId());
            return authMapper.toUserSummary(user, authorities);
        } catch (RefreshTokenException ex) {
            clearCookies(response);
            if (ex.isReuseDetected()) {
                auditService.record(AuthEventType.TOKEN_REUSE_DETECTED, null, meta);
            }
            throw ex;
        }
    }

    /** Revoke the presented refresh token and clear both cookies. */
    public void logout(HttpServletRequest request, HttpServletResponse response, UUID userId, RequestMetadata meta) {
        String refreshValue = readCookie(request, cookieProperties.getRefreshName());
        if (refreshValue != null) {
            refreshTokenService.revoke(refreshValue);
        }
        clearCookies(response);
        auditService.record(AuthEventType.LOGOUT, userId, meta);
    }

    private void issueRotated(HttpServletResponse response, String accessToken, String refreshCookieValue) {
        Duration accessMaxAge = Duration.ofMinutes(jwtProperties.getAccessTokenExpirationMinutes());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieFactory.accessCookie(accessToken, accessMaxAge).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieFactory.refreshCookie(refreshCookieValue, refreshTokenService.getTtl()).toString());
    }

    private void clearCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieFactory.clearAccessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieFactory.clearRefreshCookie().toString());
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
