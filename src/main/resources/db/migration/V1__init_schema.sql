CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    email VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(32) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_roles_role_code (role_code)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(32) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_accounts_account_no (account_no),
    KEY idx_accounts_user_id (user_id),
    KEY idx_accounts_created_at (created_at),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE account_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_no VARCHAR(40) NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_before DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_transactions_transaction_no (transaction_no),
    UNIQUE KEY uk_transactions_account_request (account_id, request_id),
    KEY idx_transactions_account_id (account_id),
    KEY idx_transactions_created_at (created_at),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(64),
    operation VARCHAR(64) NOT NULL,
    resource VARCHAR(128),
    request_method VARCHAR(16),
    request_uri VARCHAR(255),
    request_ip VARCHAR(64),
    request_id VARCHAR(64),
    result VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_user_id (user_id),
    KEY idx_audit_request_id (request_id),
    KEY idx_audit_created_at (created_at)
);
