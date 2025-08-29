-- This file contains SQL commands to create the initial database schema for the Cointoss application.

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       usdt_balance DECIMAL(10, 2) DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE betting_pools (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               asset_pair VARCHAR(50) NOT NULL,
                               status ENUM('OPEN', 'LOCKED', 'SETTLED') NOT NULL,
                               start_price DECIMAL(10, 2) NOT NULL,
                               end_price DECIMAL(10, 2),
                               total_up_pool DECIMAL(10, 2) DEFAULT 0,
                               total_down_pool DECIMAL(10, 2) DEFAULT 0,
                               open_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               lock_time TIMESTAMP,
                               settlement_time TIMESTAMP
);

CREATE TABLE bets (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      user_id BIGINT NOT NULL,
                      pool_id BIGINT NOT NULL,
                      amount DECIMAL(10, 2) NOT NULL,
                      direction ENUM('UP', 'DOWN') NOT NULL,
                      payout DECIMAL(10, 2),
                      status ENUM('PENDING', 'WON', 'LOST') NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (user_id) REFERENCES users(id),
                      FOREIGN KEY (pool_id) REFERENCES betting_pools(id)
);