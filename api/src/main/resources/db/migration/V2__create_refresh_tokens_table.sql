CREATE TABLE refresh_tokens (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL,
    token_hash      VARCHAR(64)   NOT NULL UNIQUE,
    expires_at      TIMESTAMP     NOT NULL,
    revoked_at      TIMESTAMP     NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users
      FOREIGN KEY(user_id) 
      REFERENCES users(id)
      ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);