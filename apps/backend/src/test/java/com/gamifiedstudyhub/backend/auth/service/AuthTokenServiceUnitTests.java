package com.gamifiedstudyhub.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthTokenServiceUnitTests {

    private final AuthTokenService authTokenService = new AuthTokenService(null, null);

    @Test
    void generateRawToken_shouldBeUrlSafeAndHighEntropy() {
        String token = authTokenService.generateRawToken();

        assertNotNull(token);
        assertTrue(token.matches("^[A-Za-z0-9_-]+$"));
        assertTrue(token.length() >= 43);
    }

    @Test
    void hashToken_shouldBeDeterministicAndDifferentFromRaw() {
        String rawToken = "sample-raw-token-123";
        String hash1 = authTokenService.hashToken(rawToken);
        String hash2 = authTokenService.hashToken(rawToken);

        assertEquals(hash1, hash2);
        assertNotEquals(rawToken, hash1);
        assertEquals(64, hash1.length());
    }
}
