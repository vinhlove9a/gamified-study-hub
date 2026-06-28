package com.gamifiedstudyhub.backend.mfa;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mfa")
public class MfaProperties {

    /** Base64-encoded 32-byte AES key used to encrypt TOTP secrets at rest. */
    private String encryptionKey = "";

    /** Issuer label shown in authenticator apps. */
    private String issuer = "Gamified Study Hub";

    /** Number of recovery codes generated on enrollment. */
    private int recoveryCodeCount = 8;

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getRecoveryCodeCount() {
        return recoveryCodeCount;
    }

    public void setRecoveryCodeCount(int recoveryCodeCount) {
        this.recoveryCodeCount = recoveryCodeCount;
    }
}
