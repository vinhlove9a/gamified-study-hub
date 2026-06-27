package com.gamifiedstudyhub.backend.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

    /** From address for outbound mail. */
    private String from = "no-reply@aquvix.site";

    /** Public base URL used to build verify/reset links. */
    private String baseUrl = "http://localhost:5173";

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
