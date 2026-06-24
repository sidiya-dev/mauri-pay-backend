-- A default callback URL for the merchant (used when a payment request doesn't supply one).
ALTER TABLE merchant
    ADD COLUMN callback_url VARCHAR(500);

-- Internal balance for merchants: credited when one of their payment codes is paid.
CREATE TABLE merchant_account (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID           NOT NULL UNIQUE REFERENCES merchant (id),
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency    VARCHAR(3)     NOT NULL DEFAULT 'MRU',
    version     BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT merchant_balance_non_negative CHECK (balance >= 0)
);
