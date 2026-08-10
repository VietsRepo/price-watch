CREATE TABLE users (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)  NOT NULL,
    username        VARCHAR(30),
    password        VARCHAR(255)  NOT NULL,
    role            VARCHAR(16)   NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    account_locked  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    /* 
      For custom constraint name
    */
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);