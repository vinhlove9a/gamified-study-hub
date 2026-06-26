package com.gamifiedstudyhub.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tunable login rate-limit + account-lockout policy (Redis-backed). */
@ConfigurationProperties(prefix = "app.security")
public class SecurityPolicyProperties {

    private final RateLimit ratelimit = new RateLimit();
    private final Lockout lockout = new Lockout();

    public RateLimit getRatelimit() {
        return ratelimit;
    }

    public Lockout getLockout() {
        return lockout;
    }

    public static class RateLimit {
        /** Max login attempts per IP per minute. */
        private int ipPerMinute = 30;
        /** Max login attempts per account within the window. */
        private int accountPerWindow = 5;
        /** Sliding window length for the per-account counter. */
        private long accountWindowSeconds = 900;

        public int getIpPerMinute() {
            return ipPerMinute;
        }

        public void setIpPerMinute(int ipPerMinute) {
            this.ipPerMinute = ipPerMinute;
        }

        public int getAccountPerWindow() {
            return accountPerWindow;
        }

        public void setAccountPerWindow(int accountPerWindow) {
            this.accountPerWindow = accountPerWindow;
        }

        public long getAccountWindowSeconds() {
            return accountWindowSeconds;
        }

        public void setAccountWindowSeconds(long accountWindowSeconds) {
            this.accountWindowSeconds = accountWindowSeconds;
        }
    }

    public static class Lockout {
        /** Consecutive failures before the account is locked. */
        private int maxAttempts = 5;
        /** Base lock duration; doubles each lock tier (exponential backoff). */
        private long baseLockSeconds = 60;
        /** Cap on the backoff tier exponent. */
        private int maxTier = 5;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBaseLockSeconds() {
            return baseLockSeconds;
        }

        public void setBaseLockSeconds(long baseLockSeconds) {
            this.baseLockSeconds = baseLockSeconds;
        }

        public int getMaxTier() {
            return maxTier;
        }

        public void setMaxTier(int maxTier) {
            this.maxTier = maxTier;
        }
    }
}
