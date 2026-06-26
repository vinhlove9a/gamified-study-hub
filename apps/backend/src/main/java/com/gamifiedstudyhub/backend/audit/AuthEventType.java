package com.gamifiedstudyhub.backend.audit;

/** Auth/authz audit event types (kept in sync with the V5 CHECK constraint). */
public enum AuthEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    ACCOUNT_LOCKED,
    RATE_LIMITED,
    TOKEN_REFRESH,
    TOKEN_REUSE_DETECTED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    EMAIL_VERIFIED,
    REGISTERED,
    MFA_ENROLLED,
    MFA_VERIFIED,
    MFA_FAILED,
    OAUTH_LOGIN,
    ROLE_GRANTED,
    ROLE_REVOKED
}
