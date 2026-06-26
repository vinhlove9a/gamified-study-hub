-- Append-only audit trail for authentication/authorization events.

CREATE TABLE auth_audit_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID,
    event_type VARCHAR(64) NOT NULL,
    ip VARCHAR(64),
    user_agent TEXT,
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_auth_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_auth_audit_events_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_auth_audit_event_type CHECK (event_type IN (
        'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT', 'ACCOUNT_LOCKED', 'RATE_LIMITED',
        'TOKEN_REFRESH', 'TOKEN_REUSE_DETECTED',
        'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET_COMPLETED',
        'EMAIL_VERIFIED', 'REGISTERED',
        'MFA_ENROLLED', 'MFA_VERIFIED', 'MFA_FAILED',
        'OAUTH_LOGIN', 'ROLE_GRANTED', 'ROLE_REVOKED'
    ))
);

CREATE INDEX idx_auth_audit_events_user_id ON auth_audit_events (user_id);
CREATE INDEX idx_auth_audit_events_event_type ON auth_audit_events (event_type);
CREATE INDEX idx_auth_audit_events_created_at ON auth_audit_events (created_at);
