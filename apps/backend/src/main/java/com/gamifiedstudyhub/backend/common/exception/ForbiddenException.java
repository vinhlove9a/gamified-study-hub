package com.gamifiedstudyhub.backend.common.exception;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCodes.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }
}
