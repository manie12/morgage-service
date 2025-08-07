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