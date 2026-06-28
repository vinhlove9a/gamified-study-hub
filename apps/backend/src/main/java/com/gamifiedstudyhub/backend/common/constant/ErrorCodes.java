package com.gamifiedstudyhub.backend.common.constant;

public final class ErrorCodes {

    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String USER_DISABLED = "USER_DISABLED";
    public static final String USER_PENDING = "USER_PENDING";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "AUTH_EMAIL_ALREADY_EXISTS";
    public static final String AUTH_EMAIL_NOT_VERIFIED = "AUTH_EMAIL_NOT_VERIFIED";
    public static final String AUTH_ACCOUNT_DISABLED = "AUTH_ACCOUNT_DISABLED";
    public static final String AUTH_INVALID_TOKEN = "AUTH_INVALID_TOKEN";
    public static final String AUTH_EXPIRED_TOKEN = "AUTH_EXPIRED_TOKEN";
    public static final String AUTH_PASSWORD_WEAK = "AUTH_PASSWORD_WEAK";
    public static final String AUTH_PASSWORD_RESET_TOKEN_INVALID = "AUTH_PASSWORD_RESET_TOKEN_INVALID";
    public static final String AUTH_EMAIL_VERIFICATION_TOKEN_INVALID = "AUTH_EMAIL_VERIFICATION_TOKEN_INVALID";
    public static final String AUTH_RATE_LIMITED = "AUTH_RATE_LIMITED";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";
    public static final String AUTH_REFRESH_INVALID = "AUTH_REFRESH_INVALID";
    public static final String AUTH_MFA_INVALID_CODE = "AUTH_MFA_INVALID_CODE";
    public static final String AUTH_MFA_ALREADY_ENROLLED = "AUTH_MFA_ALREADY_ENROLLED";
    public static final String AUTH_MFA_NOT_ENROLLED = "AUTH_MFA_NOT_ENROLLED";
    public static final String AUTH_MFA_CHALLENGE_INVALID = "AUTH_MFA_CHALLENGE_INVALID";
    public static final String DOCUMENT_NOT_FOUND = "DOCUMENT_NOT_FOUND";
    public static final String DOCUMENT_CATEGORY_NOT_FOUND = "DOCUMENT_CATEGORY_NOT_FOUND";
    public static final String DOCUMENT_VALIDATION_ERROR = "DOCUMENT_VALIDATION_ERROR";

    private ErrorCodes() {
    }
}
