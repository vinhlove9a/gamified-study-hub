CREATE TABLE users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING'))
);

CREATE TABLE workspaces (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url TEXT,
    primary_color VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    owner_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_workspaces PRIMARY KEY (id),
    CONSTRAINT fk_workspaces_owner_user FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT chk_workspaces_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE TABLE workspace_members (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMPTZ,
    invited_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_workspace_members PRIMARY KEY (id),
    CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_workspace_members_role CHECK (role IN ('OWNER', 'ADMIN', 'TEACHER', 'STUDENT')),
    CONSTRAINT chk_workspace_members_status CHECK (status IN ('ACTIVE', 'INVITED', 'DISABLED'))
);

CREATE TABLE workspace_settings (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_workspace_settings PRIMARY KEY (id),
    CONSTRAINT fk_workspace_settings_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT uq_workspace_settings_workspace_key UNIQUE (workspace_id, setting_key)
);

CREATE TABLE document_categories (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    parent_id UUID,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_document_categories PRIMARY KEY (id),
    CONSTRAINT fk_document_categories_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_document_categories_parent FOREIGN KEY (parent_id) REFERENCES document_categories (id),
    CONSTRAINT fk_document_categories_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE documents (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    category_id UUID,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    original_file_name VARCHAR(500),
    storage_key TEXT,
    file_url TEXT,
    file_type VARCHAR(100),
    file_size BIGINT,
    coin_price INTEGER NOT NULL DEFAULT 0,
    visibility VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    uploaded_by UUID,
    download_count BIGINT NOT NULL DEFAULT 0,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_documents PRIMARY KEY (id),
    CONSTRAINT fk_documents_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_documents_category FOREIGN KEY (category_id) REFERENCES document_categories (id),
    CONSTRAINT fk_documents_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id),
    CONSTRAINT chk_documents_coin_price_non_negative CHECK (coin_price >= 0),
    CONSTRAINT chk_documents_file_size_non_negative CHECK (file_size IS NULL OR file_size >= 0),
    CONSTRAINT chk_documents_download_count_non_negative CHECK (download_count >= 0),
    CONSTRAINT chk_documents_visibility CHECK (visibility IN ('FREE', 'VIP', 'PRIVATE')),
    CONSTRAINT chk_documents_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE document_tags (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_document_tags PRIMARY KEY (id),
    CONSTRAINT fk_document_tags_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
);

CREATE TABLE document_tag_relations (
    workspace_id UUID NOT NULL,
    document_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_document_tag_relations PRIMARY KEY (document_id, tag_id),
    CONSTRAINT fk_document_tag_relations_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_document_tag_relations_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_document_tag_relations_tag FOREIGN KEY (tag_id) REFERENCES document_tags (id)
);

CREATE TABLE coin_wallets (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    balance INTEGER NOT NULL DEFAULT 0,
    total_earned INTEGER NOT NULL DEFAULT 0,
    total_spent INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_coin_wallets PRIMARY KEY (id),
    CONSTRAINT fk_coin_wallets_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_coin_wallets_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_coin_wallets_workspace_user UNIQUE (workspace_id, user_id),
    CONSTRAINT chk_coin_wallets_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_coin_wallets_total_earned_non_negative CHECK (total_earned >= 0),
    CONSTRAINT chk_coin_wallets_total_spent_non_negative CHECK (total_spent >= 0)
);

CREATE TABLE coin_transactions (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    type VARCHAR(100) NOT NULL,
    amount INTEGER NOT NULL,
    balance_before INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    reference_type VARCHAR(100),
    reference_id UUID,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_coin_transactions PRIMARY KEY (id),
    CONSTRAINT fk_coin_transactions_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_coin_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_coin_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES coin_wallets (id),
    CONSTRAINT chk_coin_transactions_type CHECK (
        type IN (
            'DAILY_CHECKIN_REWARD',
            'MISSION_REWARD',
            'DOCUMENT_UNLOCK',
            'ADMIN_ADJUSTMENT',
            'CONTRIBUTION_REWARD'
        )
    ),
    CONSTRAINT chk_coin_transactions_amount_non_zero CHECK (amount <> 0),
    CONSTRAINT chk_coin_transactions_balance_before_non_negative CHECK (balance_before >= 0),
    CONSTRAINT chk_coin_transactions_balance_after_non_negative CHECK (balance_after >= 0)
);

CREATE TABLE missions (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(100) NOT NULL,
    coin_reward INTEGER NOT NULL DEFAULT 0,
    max_completions_per_user INTEGER NOT NULL DEFAULT 1,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_missions PRIMARY KEY (id),
    CONSTRAINT fk_missions_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_missions_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_missions_type CHECK (type IN ('DAILY_CHECKIN', 'COMPLETE_TASK', 'UPLOAD_CONTRIBUTION', 'MANUAL')),
    CONSTRAINT chk_missions_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_missions_coin_reward_non_negative CHECK (coin_reward >= 0),
    CONSTRAINT chk_missions_max_completions CHECK (max_completions_per_user >= 1),
    CONSTRAINT chk_missions_time_window CHECK (end_at IS NULL OR start_at IS NULL OR end_at >= start_at)
);

CREATE TABLE mission_completions (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    mission_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reward_coins INTEGER NOT NULL DEFAULT 0,
    metadata JSONB,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_mission_completions PRIMARY KEY (id),
    CONSTRAINT fk_mission_completions_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_mission_completions_mission FOREIGN KEY (mission_id) REFERENCES missions (id),
    CONSTRAINT fk_mission_completions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_mission_completions_reward_non_negative CHECK (reward_coins >= 0)
);

CREATE TABLE document_unlocks (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    document_id UUID NOT NULL,
    user_id UUID NOT NULL,
    coin_spent INTEGER NOT NULL DEFAULT 0,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_document_unlocks PRIMARY KEY (id),
    CONSTRAINT fk_document_unlocks_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_document_unlocks_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_document_unlocks_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_document_unlocks_document_user UNIQUE (document_id, user_id),
    CONSTRAINT chk_document_unlocks_coin_spent_non_negative CHECK (coin_spent >= 0)
);

CREATE TABLE download_logs (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    document_id UUID NOT NULL,
    user_id UUID,
    ip_address VARCHAR(100),
    user_agent TEXT,
    downloaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_download_logs PRIMARY KEY (id),
    CONSTRAINT fk_download_logs_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_download_logs_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_download_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_checkins (
    id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    checkin_date DATE NOT NULL,
    reward_coins INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_daily_checkins PRIMARY KEY (id),
    CONSTRAINT fk_daily_checkins_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_daily_checkins_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_daily_checkins_workspace_user_date UNIQUE (workspace_id, user_id, checkin_date),
    CONSTRAINT chk_daily_checkins_reward_non_negative CHECK (reward_coins >= 0)
);

CREATE UNIQUE INDEX ux_users_email_active ON users (lower(email)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_workspaces_slug_active ON workspaces (slug) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_workspace_members_workspace_user_active ON workspace_members (workspace_id, user_id)
    WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_document_categories_workspace_slug_active ON document_categories (workspace_id, slug)
    WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_documents_workspace_slug_active ON documents (workspace_id, slug)
    WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_document_tags_workspace_slug_active ON document_tags (workspace_id, slug)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_created_at ON users (created_at);

CREATE INDEX idx_workspaces_slug ON workspaces (slug);
CREATE INDEX idx_workspaces_status ON workspaces (status);
CREATE INDEX idx_workspaces_owner_user_id ON workspaces (owner_user_id);
CREATE INDEX idx_workspaces_created_at ON workspaces (created_at);

CREATE INDEX idx_workspace_members_workspace_id ON workspace_members (workspace_id);
CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);
CREATE INDEX idx_workspace_members_role ON workspace_members (role);
CREATE INDEX idx_workspace_members_status ON workspace_members (status);
CREATE INDEX idx_workspace_members_workspace_user ON workspace_members (workspace_id, user_id);

CREATE INDEX idx_workspace_settings_workspace_id ON workspace_settings (workspace_id);
CREATE INDEX idx_workspace_settings_setting_key ON workspace_settings (setting_key);

CREATE INDEX idx_document_categories_workspace_id ON document_categories (workspace_id);
CREATE INDEX idx_document_categories_parent_id ON document_categories (parent_id);
CREATE INDEX idx_document_categories_slug ON document_categories (slug);
CREATE INDEX idx_document_categories_sort_order ON document_categories (sort_order);

CREATE INDEX idx_documents_workspace_id ON documents (workspace_id);
CREATE INDEX idx_documents_category_id ON documents (category_id);
CREATE INDEX idx_documents_uploaded_by ON documents (uploaded_by);
CREATE INDEX idx_documents_visibility ON documents (visibility);
CREATE INDEX idx_documents_status ON documents (status);
CREATE INDEX idx_documents_created_at ON documents (created_at);
CREATE INDEX idx_documents_published_at ON documents (published_at);
CREATE INDEX idx_documents_workspace_status ON documents (workspace_id, status);
CREATE INDEX idx_documents_workspace_visibility ON documents (workspace_id, visibility);
CREATE INDEX idx_documents_title_search ON documents USING gin (to_tsvector('simple', title));

CREATE INDEX idx_document_tags_workspace_id ON document_tags (workspace_id);
CREATE INDEX idx_document_tags_slug ON document_tags (slug);

CREATE INDEX idx_document_tag_relations_workspace_id ON document_tag_relations (workspace_id);
CREATE INDEX idx_document_tag_relations_document_id ON document_tag_relations (document_id);
CREATE INDEX idx_document_tag_relations_tag_id ON document_tag_relations (tag_id);

CREATE INDEX idx_coin_wallets_workspace_id ON coin_wallets (workspace_id);
CREATE INDEX idx_coin_wallets_user_id ON coin_wallets (user_id);
CREATE INDEX idx_coin_wallets_workspace_user ON coin_wallets (workspace_id, user_id);

CREATE INDEX idx_coin_transactions_workspace_id ON coin_transactions (workspace_id);
CREATE INDEX idx_coin_transactions_user_id ON coin_transactions (user_id);
CREATE INDEX idx_coin_transactions_wallet_id ON coin_transactions (wallet_id);
CREATE INDEX idx_coin_transactions_type ON coin_transactions (type);
CREATE INDEX idx_coin_transactions_created_at ON coin_transactions (created_at);
CREATE INDEX idx_coin_transactions_reference ON coin_transactions (reference_type, reference_id);

CREATE INDEX idx_missions_workspace_id ON missions (workspace_id);
CREATE INDEX idx_missions_type ON missions (type);
CREATE INDEX idx_missions_status ON missions (status);
CREATE INDEX idx_missions_start_at ON missions (start_at);
CREATE INDEX idx_missions_end_at ON missions (end_at);
CREATE INDEX idx_missions_created_by ON missions (created_by);

CREATE INDEX idx_mission_completions_workspace_id ON mission_completions (workspace_id);
CREATE INDEX idx_mission_completions_mission_id ON mission_completions (mission_id);
CREATE INDEX idx_mission_completions_user_id ON mission_completions (user_id);
CREATE INDEX idx_mission_completions_completed_at ON mission_completions (completed_at);
CREATE INDEX idx_mission_completions_workspace_user ON mission_completions (workspace_id, user_id);
CREATE INDEX idx_mission_completions_mission_user ON mission_completions (mission_id, user_id);

CREATE INDEX idx_document_unlocks_workspace_id ON document_unlocks (workspace_id);
CREATE INDEX idx_document_unlocks_document_id ON document_unlocks (document_id);
CREATE INDEX idx_document_unlocks_user_id ON document_unlocks (user_id);
CREATE INDEX idx_document_unlocks_unlocked_at ON document_unlocks (unlocked_at);

CREATE INDEX idx_download_logs_workspace_id ON download_logs (workspace_id);
CREATE INDEX idx_download_logs_document_id ON download_logs (document_id);
CREATE INDEX idx_download_logs_user_id ON download_logs (user_id);
CREATE INDEX idx_download_logs_downloaded_at ON download_logs (downloaded_at);

CREATE INDEX idx_daily_checkins_workspace_id ON daily_checkins (workspace_id);
CREATE INDEX idx_daily_checkins_user_id ON daily_checkins (user_id);
CREATE INDEX idx_daily_checkins_checkin_date ON daily_checkins (checkin_date);
CREATE INDEX idx_daily_checkins_workspace_user_date ON daily_checkins (workspace_id, user_id, checkin_date);
