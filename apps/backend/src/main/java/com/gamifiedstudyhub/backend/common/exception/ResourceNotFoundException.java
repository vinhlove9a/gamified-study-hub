package com.gamifiedstudyhub.backend.common.exception;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
