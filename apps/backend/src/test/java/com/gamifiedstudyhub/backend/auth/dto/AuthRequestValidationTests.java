package com.gamifiedstudyhub.backend.auth.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthRequestValidationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerRequest_shouldFailValidationWhenFieldsAreInvalid() {
        RegisterRequest request = new RegisterRequest("", "", "123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "password".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "fullName".equals(v.getPropertyPath().toString())));
    }

    @Test
    void loginRequest_shouldFailValidationWhenFieldsAreInvalid() {
        LoginRequest request = new LoginRequest("invalid", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "password".equals(v.getPropertyPath().toString())));
    }
}
