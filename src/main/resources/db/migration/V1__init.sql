CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone         VARCHAR(32)  NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE account (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID          NOT NULL UNIQUE REFERENCES app_user (id),
    balance   NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency  VARCHAR(3)    NOT NULL DEFAULT 'MRU',
    version   BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE merchant (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(120) NOT NULL,
    api_key_hash   VARCHAR(100) NOT NULL UNIQUE,
    webhook_secret VARCHAR(100) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE payment_request (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code             VARCHAR(16)   NOT NULL UNIQUE,
    merchant_id      UUID          NOT NULL REFERENCES merchant (id),
    amount           NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'MRU',
    status           VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    order_ref        VARCHAR(120),
    callback_url     VARCHAR(500),
    paid_by_user_id  UUID          REFERENCES app_user (id),
    paid_at          TIMESTAMPTZ,
    expires_at       TIMESTAMPTZ   NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_payment_request_status ON payment_request (status);

CREATE TABLE ledger_transaction (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id           UUID          NOT NULL REFERENCES app_user (id),
    receiver_id         UUID          REFERENCES app_user (id),
    amount              NUMERIC(19, 4) NOT NULL,
    transaction_type    VARCHAR(32)   NOT NULL,
    payment_request_id  UUID          REFERENCES payment_request (id),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE webhook_delivery (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_request_id  UUID          NOT NULL REFERENCES payment_request (id),
    url                 VARCHAR(500)  NOT NULL,
    payload             TEXT          NOT NULL,
    signature           VARCHAR(128)  NOT NULL,
    status              VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    attempts            INT           NOT NULL DEFAULT 0,
    next_attempt_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_error          TEXT,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_webhook_delivery_pending ON webhook_delivery (status, next_attempt_at);
