CREATE SCHEMA IF NOT EXISTS mortgage;

SET search_path TO mortgage;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE mortgage.applications
(
    id                UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    version           INT            NOT NULL DEFAULT 0,
    external_ref      VARCHAR(255),
    user_id           VARCHAR(255)   NOT NULL,
    public_id         VARCHAR(255)   NOT NULL,
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
    created_at        VARCHAR(255)   NOT NULL,
    updated_at        VARCHAR(255),
    soft_deleted      BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE TABLE mortgage.documents
(
    id             UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id        VARCHAR(255)  NOT NULL,
    public_id      VARCHAR(255)  NOT NULL,
    type           VARCHAR(50)   NOT NULL,
    file_name      TEXT          NOT NULL,
    content_type   VARCHAR(100)  NOT NULL,
    size_bytes     BIGINT        NOT NULL CHECK (size_bytes > 0),
    upload_url     TEXT          NOT NULL,
    checksum       VARCHAR(128),
    document_status VARCHAR(30)  NOT NULL DEFAULT 'PENDING_UPLOAD',
    created_at     VARCHAR(255)  NOT NULL,
    updated_at     VARCHAR(255)
);

CREATE TABLE mortgage.decisions
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id         VARCHAR(255)  NOT NULL,
    public_id       VARCHAR(255)  NOT NULL,
    decision_type   VARCHAR(20)   NOT NULL,
    officer_user_id VARCHAR(100)  NOT NULL,
    officer_name    VARCHAR(200),
    comments        TEXT,
    created_at      VARCHAR(255)  NOT NULL,
    decided_at      VARCHAR(255)
);

-- Outbox for transactional publishing
CREATE TABLE mortgage.outbox_events
(
    id             UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    headers        TEXT,
    occurred_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMP,
    attempts       INT          NOT NULL DEFAULT 0
);

CREATE TABLE mortgage.users (
                                id              BIGSERIAL PRIMARY KEY,
                                public_id       VARCHAR(255) NOT NULL UNIQUE,
                                national_id     VARCHAR(255) NOT NULL UNIQUE,
                                password_hash   VARCHAR(255) NOT NULL,
                                created_at      VARCHAR(255) NOT NULL,
                                updated_at      VARCHAR(255),
                                created_by      VARCHAR(255)
);

-- Recommended indexes
CREATE INDEX idx_users_created_at ON mortgage.users (created_at);

CREATE TABLE mortgage.roles (
                                id          SERIAL PRIMARY KEY,
                                name        VARCHAR(50) NOT NULL,
                                description VARCHAR(255),
                                user_id     VARCHAR(255),
                                created_at  VARCHAR(255),
                                updated_at  VARCHAR(255)
);

CREATE TABLE mortgage.user_roles (
                                     user_id BIGINT NOT NULL REFERENCES mortgage.users(id) ON DELETE CASCADE,
                                     role_id INT    NOT NULL REFERENCES mortgage.roles(id) ON DELETE CASCADE,
                                     PRIMARY KEY (user_id, role_id)
);

/* optional helper index for role look-ups */
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON mortgage.user_roles(role_id);

CREATE TABLE mortgage.refresh_tokens (
                                         token       UUID PRIMARY KEY,
                                         user_id     BIGINT       NOT NULL REFERENCES mortgage.users(id) ON DELETE CASCADE,
                                         expires_at  TIMESTAMP    NOT NULL,
                                         revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
                                         created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_user ON mortgage.refresh_tokens(user_id);