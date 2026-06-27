package com.gamifiedstudyhub.backend.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Auth cookie attributes. {@code domain} empty = host-only (localhost dev). */
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {

    private String domain = "";
    private boolean secure = true;
    private String sameSite = "Lax";

    /** Access-token cookie name (sent on every request, path "/"). */
    private String accessName = "gsh_at";
    /** Refresh-token cookie name (scoped to the auth path). */
    private String refreshName = "gsh_rt";
    /** Path the refresh cookie is scoped to. */
    private String refreshPath = "/api/v1/auth";

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    public String getAccessName() {
        return accessName;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public String getRefreshName() {
        return refreshName;
    }

    public void setRefreshName(String refreshName) {
        this.refreshName = refreshName;
    }

    public String getRefreshPath() {
        return refreshPath;
    }

    public void setRefreshPath(String refreshPath) {
        this.refreshPath = refreshPath;
    }
}
