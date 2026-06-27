package com.gamifiedstudyhub.backend.auth.security;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Builds the httpOnly access/refresh cookies (and their clear variants). */
@Component
public class AuthCookieFactory {

    private final CookieProperties props;

    public AuthCookieFactory(CookieProperties props) {
        this.props = props;
    }

    public ResponseCookie accessCookie(String token, Duration maxAge) {
        return base(props.getAccessName(), token, "/", maxAge).build();
    }

    public ResponseCookie refreshCookie(String token, Duration maxAge) {
        return base(props.getRefreshName(), token, props.getRefreshPath(), maxAge).build();
    }

    public ResponseCookie clearAccessCookie() {
        return base(props.getAccessName(), "", "/", Duration.ZERO).build();
    }

    public ResponseCookie clearRefreshCookie() {
        return base(props.getRefreshName(), "", props.getRefreshPath(), Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String name, String value, String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path(path)
                .maxAge(maxAge);
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }
        return builder;
    }
}
