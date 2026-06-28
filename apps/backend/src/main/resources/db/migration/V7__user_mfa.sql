-- TOTP multi-factor authentication: one config row per user + recovery codes.

CREATE TABLE user_mfa (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(16) NOT NULL DEFAULT 'TOTP',
    secret_encrypted TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    enrolled_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_mfa PRIMARY KEY (id),
    CONSTRAINT fk_user_mfa_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_mfa_user UNIQUE (user_id),
    CONSTRAINT chk_user_mfa_type CHECK (type IN ('TOTP'))
);

CREATE TABLE user_mfa_recovery_codes (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_mfa_id UUID NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_mfa_recovery_codes PRIMARY KEY (id),
    CONSTRAINT fk_user_mfa_recovery_codes_mfa FOREIGN KEY (user_mfa_id)
        REFERENCES user_mfa (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_mfa_recovery_codes_mfa_id ON user_mfa_recovery_codes (user_mfa_id);
