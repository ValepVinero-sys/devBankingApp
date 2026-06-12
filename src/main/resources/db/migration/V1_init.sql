CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     first_name VARCHAR(100),
                                     last_name VARCHAR(100),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS accounts (
                                        id BIGSERIAL PRIMARY KEY,
                                        account_number VARCHAR(20) NOT NULL UNIQUE,
                                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                        currency VARCHAR(3) DEFAULT 'RUB',
                                        type VARCHAR(20) DEFAULT 'CHECKING',
                                        active BOOLEAN DEFAULT TRUE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS transactions (
                                            id BIGSERIAL PRIMARY KEY,
                                            transaction_id VARCHAR(36) UNIQUE,
                                            from_account_id BIGINT REFERENCES accounts(id),
                                            to_account_id BIGINT REFERENCES accounts(id),
                                            amount DECIMAL(15,2) NOT NULL,
                                            type VARCHAR(20) NOT NULL,
                                            status VARCHAR(20) DEFAULT 'PENDING',
                                            description TEXT,
                                            fee DECIMAL(15,2),
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            completed_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account ON transactions(to_account_id);
