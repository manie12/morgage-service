CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE applications
(
    id                UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    version           INT            NOT NULL DEFAULT 0,
    external_ref      VARCHAR(100),
    applicant_user_id VARCHAR(100)   NOT NULL,
    national_id_hash  VARCHAR(128)   NOT NULL, -- HMAC-SHA256 hex for equality search
    national_id_enc   BYTEA          NOT NULL, -- encrypted bytes for retrieval (optional)
    loan_amount       NUMERIC(18, 2) NOT NULL CHECK (loan_amount > 0),
    currency          VARCHAR(3)     NOT NULL,
    income            NUMERIC(18, 2)          DEFAULT 0,
    liabilities       NUMERIC(18, 2)          DEFAULT 0,
    property_address  TEXT           NOT NULL,
    property_value    NUMERIC(18, 2)          DEFAULT 0,
    property_type     VARCHAR(50)    NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    submitted_at      TIMESTAMPTZ,
    decided_at        TIMESTAMPTZ,
    soft_deleted      BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE TABLE documents
(
    id             UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    application_id UUID         NOT NULL REFERENCES applications (id),
    type           VARCHAR(50)  NOT NULL,
    file_name      TEXT         NOT NULL,
    content_type   VARCHAR(100) NOT NULL,
    size_bytes     BIGINT       NOT NULL CHECK (size_bytes > 0),
    upload_url     TEXT         NOT NULL,
    checksum       VARCHAR(128),
    status         VARCHAR(30)  NOT NULL DEFAULT 'PENDING_UPLOAD',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE decisions
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    application_id  UUID         NOT NULL REFERENCES applications (id),
    decision        VARCHAR(20)  NOT NULL,
    officer_user_id VARCHAR(100) NOT NULL,
    officer_name    VARCHAR(200),
    comments        TEXT,
    decided_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Outbox for transactional publishing
CREATE TABLE outbox_events
(
    id             UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB        NOT NULL,
    headers        JSONB        NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ,
    attempts       INT          NOT NULL DEFAULT 0
);

/* ---------------------------------------------------------------------------
   V4__auth_tables.sql   –  Normalize roles and add refresh_tokens
   ---------------------------------------------------------------------------
   ▸ removes single role column from users
   ▸ creates roles, user_roles, refresh_tokens
   ▸ seeds standard roles (APPLICANT, OFFICER)
   -------------------------------------------------------------------------*/
-- V3__create_users.sql  (Flyway naming example)

CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       national_id     VARCHAR(255) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       role            VARCHAR(50)  NOT NULL CHECK (role IN ('APPLICANT', 'OFFICER')),
                       created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Recommended indexes
CREATE INDEX idx_users_role          ON users (role);
CREATE INDEX idx_users_created_at    ON users (created_at DESC);
/* 1️⃣  drop old single-role column (leave data migration to app/admin) */
ALTER TABLE users
DROP COLUMN IF EXISTS role;

/* 2️⃣  roles lookup */
CREATE TABLE roles (
                       id   SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL            -- APPLICANT, OFFICER
);

/* seed base RBAC roles */
INSERT INTO roles (name) VALUES ('APPLICANT'), ('OFFICER')
    ON CONFLICT DO NOTHING;

/* 3️⃣  association table (many-to-many) */
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
                            role_id INT    NOT NULL REFERENCES roles(id)  ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

/* optional helper index for role look-ups */
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);

/* 4️⃣  persistent refresh tokens */
CREATE TABLE refresh_tokens (
                                token       UUID PRIMARY KEY,                -- opaque, random
                                user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expires_at  TIMESTAMPTZ  NOT NULL,
                                revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);

