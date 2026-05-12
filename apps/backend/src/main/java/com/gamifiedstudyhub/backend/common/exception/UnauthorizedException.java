package com.gamifiedstudyhub.backend.common.exception;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCodes.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }
}
