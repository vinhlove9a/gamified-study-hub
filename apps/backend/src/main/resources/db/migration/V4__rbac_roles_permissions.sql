-- RBAC: global roles + permissions, role↔permission and user↔role mappings.
-- Workspace-scoped roles continue to live in workspace_members (V2); these are
-- the APPLICATION-wide roles used to grant Spring Security authorities.

CREATE TABLE roles (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uq_permissions_code UNIQUE (code)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    granted_by UUID,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_granted_by FOREIGN KEY (granted_by) REFERENCES users (id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);

-- Seed roles (codes carry the Spring Security ROLE_ prefix).
INSERT INTO roles (code, name, description, is_system) VALUES
    ('ROLE_USER',  'User',          'Default authenticated user', true),
    ('ROLE_ADMIN', 'Administrator', 'Full platform administrator', true)
ON CONFLICT (code) DO NOTHING;

-- Seed permissions.
INSERT INTO permissions (code, description) VALUES
    ('USER_READ',        'Read own/user profile data'),
    ('USER_MANAGE',      'Manage users (list, disable, assign roles)'),
    ('DOCUMENT_READ',    'Read documents'),
    ('DOCUMENT_PUBLISH', 'Publish documents'),
    ('DOCUMENT_MANAGE',  'Manage any document'),
    ('WORKSPACE_MANAGE', 'Manage workspaces and members'),
    ('ADMIN_PANEL',      'Access the admin dashboard')
ON CONFLICT (code) DO NOTHING;

-- ROLE_ADMIN -> every permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- ROLE_USER -> read-only baseline.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN ('USER_READ', 'DOCUMENT_READ')
WHERE r.code = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- Backfill: every existing (non-deleted) user becomes ROLE_USER.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE code = 'ROLE_USER')
FROM users u
WHERE u.deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- NOTE: to promote an account to admin, run once after deploy (no hardcoded admin here):
--   INSERT INTO user_roles (user_id, role_id)
--   SELECT u.id, r.id FROM users u, roles r
--   WHERE lower(u.email) = lower('admin@example.com') AND r.code = 'ROLE_ADMIN'
--   ON CONFLICT DO NOTHING;
