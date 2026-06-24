-- Brute-force protection: track consecutive failed logins and a temporary lock window.
ALTER TABLE app_user
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until    TIMESTAMPTZ;
