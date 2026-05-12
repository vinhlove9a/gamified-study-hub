package com.gamifiedstudyhub.backend.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String code,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDetail> fieldErrors
) {
    public ErrorResponse(String code, String message, String path, List<FieldErrorDetail> fieldErrors) {
        this(false, code, message, path, Instant.now(), fieldErrors == null ? List.of() : List.copyOf(fieldErrors));
    }
}
