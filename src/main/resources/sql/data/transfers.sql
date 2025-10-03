CREATE TABLE transfers
(
    id            BIGINT PRIMARY KEY,
    source_id     BIGINT    NOT NULL,
    target_id     BIGINT    NOT NULL,
    amount        DOUBLE NOT NULL,
    transfer_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_source FOREIGN KEY (source_id) REFERENCES accounts (id),
    CONSTRAINT fk_target FOREIGN KEY (target_id) REFERENCES accounts (id),
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_different_accounts CHECK (source_id != target_id)
    );

-- Composite index for efficient querying by source and time
CREATE INDEX idx_transfers_source_time ON transfers (source_id, transfer_time);