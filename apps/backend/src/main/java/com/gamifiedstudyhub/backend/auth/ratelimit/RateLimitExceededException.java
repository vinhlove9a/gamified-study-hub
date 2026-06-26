package com.gamifiedstudyhub.backend.auth.ratelimit;

import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/** Raised when login is throttled (IP/account rate limit) or the account is locked. */
public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException(String code, String message) {
        super(code, message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
