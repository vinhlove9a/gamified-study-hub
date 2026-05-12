package com.gamifiedstudyhub.backend.common.exception;

public record FieldErrorDetail(String field, String message, Object rejectedValue) {
}
