package com.gamifiedstudyhub.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PasswordPolicyValidatorTests {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void isValid_shouldAcceptPasswordWithLettersAndNumbers() {
        assertTrue(validator.isValid("Password123"));
    }

    @Test
    void isValid_shouldRejectWeakPasswords() {
        assertFalse(validator.isValid("short1"));
        assertFalse(validator.isValid("onlyletters"));
        assertFalse(validator.isValid("12345678"));
        assertFalse(validator.isValid("   "));
    }

    @Test
    void validateOrThrow_shouldThrowBusinessExceptionForWeakPassword() {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateOrThrow("weak"));
        assertEquals(ErrorCodes.AUTH_PASSWORD_WEAK, exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void validateOrThrow_shouldPassForValidPassword() {
        assertDoesNotThrow(() -> validator.validateOrThrow("GoodPass123"));
    }
}
