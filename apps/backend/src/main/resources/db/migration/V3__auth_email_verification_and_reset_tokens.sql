-- Auth token storage foundation for email verification and password reset flows.
-- Tokens are stored as hashes of random opaque values; raw tokens are never persisted.
-- consumed_at marks one-time usage, expires_at supports TTL validation and cleanup queries.

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMPTZ;

CREATE TABLE auth_email_verification_tokens (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_auth_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_auth_email_verification_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_auth_email_verification_tokens_expires_after_created CHECK (expires_at > created_at)
);

CREATE TABLE auth_password_reset_tokens (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_auth_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_auth_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_auth_password_reset_tokens_expires_after_created CHECK (expires_at > created_at)
);

CREATE INDEX idx_auth_email_verification_tokens_user_id ON auth_email_verification_tokens (user_id);
CREATE INDEX idx_auth_email_verification_tokens_token_hash ON auth_email_verification_tokens (token_hash);
CREATE INDEX idx_auth_email_verification_tokens_expires_at ON auth_email_verification_tokens (expires_at);
CREATE INDEX idx_auth_email_verification_tokens_active ON auth_email_verification_tokens (expires_at)
    WHERE consumed_at IS NULL;

CREATE INDEX idx_auth_password_reset_tokens_user_id ON auth_password_reset_tokens (user_id);
CREATE INDEX idx_auth_password_reset_tokens_token_hash ON auth_password_reset_tokens (token_hash);
CREATE INDEX idx_auth_password_reset_tokens_expires_at ON auth_password_reset_tokens (expires_at);
CREATE INDEX idx_auth_password_reset_tokens_active ON auth_password_reset_tokens (expires_at)
    WHERE consumed_at IS NULL;
