CREATE TABLE accounts
(
    id         BIGINT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    balance    DOUBLE NOT NULL,
    CONSTRAINT chk_balance CHECK (balance >= 0)
);