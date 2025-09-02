-- Migration to support Wallet model and remove balance from users

-- 1. Remove usdt_balance from users table
ALTER TABLE users DROP COLUMN usdt_balance;

-- 2. Create wallets table
CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 100.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'USDT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
