package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;

    public void validateOrThrow(String password) {
        if (!isValid(password)) {
            throw new BusinessException(
                    ErrorCodes.AUTH_PASSWORD_WEAK,
                    "Password must be at least 8 characters and include at least one letter and one number",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    public boolean isValid(String password) {
        if (password == null || password.isBlank() || password.length() < MIN_LENGTH) {
            return false;
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}
