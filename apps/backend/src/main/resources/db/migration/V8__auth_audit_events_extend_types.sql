-- Extend the audit event_type CHECK constraint with the Phase 1 auth events
-- (change-password, profile update, log-out-everywhere). Keep in sync with
-- com.gamifiedstudyhub.backend.audit.AuthEventType.

ALTER TABLE auth_audit_events DROP CONSTRAINT chk_auth_audit_event_type;

ALTER TABLE auth_audit_events ADD CONSTRAINT chk_auth_audit_event_type CHECK (event_type IN (
    'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT', 'ACCOUNT_LOCKED', 'RATE_LIMITED',
    'TOKEN_REFRESH', 'TOKEN_REUSE_DETECTED',
    'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET_COMPLETED',
    'PASSWORD_CHANGED', 'PROFILE_UPDATED', 'LOGOUT_ALL',
    'EMAIL_VERIFIED', 'REGISTERED',
    'MFA_ENROLLED', 'MFA_VERIFIED', 'MFA_FAILED',
    'OAUTH_LOGIN', 'ROLE_GRANTED', 'ROLE_REVOKED'
));
