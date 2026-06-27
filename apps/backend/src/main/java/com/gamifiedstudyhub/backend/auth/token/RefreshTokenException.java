package com.gamifiedstudyhub.backend.auth.token;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/** Refresh token missing, invalid, expired, or replayed. */
public class RefreshTokenException extends BusinessException {

    private final boolean reuseDetected;

    public RefreshTokenException(String message, boolean reuseDetected) {
        super(ErrorCodes.AUTH_REFRESH_INVALID, message, HttpStatus.UNAUTHORIZED);
        this.reuseDetected = reuseDetected;
    }

    public boolean isReuseDetected() {
        return reuseDetected;
    }
}
