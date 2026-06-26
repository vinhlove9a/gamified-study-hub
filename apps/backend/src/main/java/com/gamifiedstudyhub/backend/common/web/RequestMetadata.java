package com.gamifiedstudyhub.backend.common.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Client IP + User-Agent captured at the web layer so services stay decoupled
 * from the servlet API. IP honours {@code X-Forwarded-For} (the stack sits
 * behind the trusted Caddy reverse proxy; the backend port is bound to
 * 127.0.0.1 in prod so the header cannot be spoofed externally).
 */
public record RequestMetadata(String ip, String userAgent) {

    private static final int MAX_UA_LENGTH = 512;

    public static RequestMetadata from(HttpServletRequest request) {
        if (request == null) {
            return new RequestMetadata("unknown", null);
        }
        return new RequestMetadata(resolveIp(request), truncate(request.getHeader("User-Agent")));
    }

    private static String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For: client, proxy1, proxy2 — the first hop is the client.
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }

    private static String truncate(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > MAX_UA_LENGTH ? userAgent.substring(0, MAX_UA_LENGTH) : userAgent;
    }
}
