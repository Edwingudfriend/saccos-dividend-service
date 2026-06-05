-- V1__dividend_schema.sql
-- Dividend Engine Schema for SACCOS Platform

CREATE TABLE dividend_cycle (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    fiscal_year             INT             NOT NULL UNIQUE,
    total_profit            DECIMAL(18,2)   NOT NULL DEFAULT 0,
    distributable_profit    DECIMAL(18,2)   NOT NULL DEFAULT 0,
    total_shares            BIGINT          NOT NULL DEFAULT 0,
    dividend_per_share      DECIMAL(18,6)   NOT NULL DEFAULT 0,
    wht_rate                DECIMAL(5,4)    NOT NULL DEFAULT 0.05,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- PENDING | BOARD_APPROVED | PROCESSING | COMPLETED | CANCELLED
    board_approved_by       VARCHAR(100),
    board_approved_at       DATETIME,
    processed_at            DATETIME,
    notes                   TEXT,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('PENDING','BOARD_APPROVED','PROCESSING','COMPLETED','CANCELLED'))
);

CREATE TABLE dividend_allocation (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    cycle_id                BIGINT          NOT NULL,
    member_id               BIGINT          NOT NULL,
    member_number           VARCHAR(30)     NOT NULL,
    member_name             VARCHAR(200)    NOT NULL,
    shares_held             BIGINT          NOT NULL DEFAULT 0,
    gross_dividend          DECIMAL(18,2)   NOT NULL DEFAULT 0,
    wht_amount              DECIMAL(18,2)   NOT NULL DEFAULT 0,
    net_dividend            DECIMAL(18,2)   NOT NULL DEFAULT 0,
    fineract_savings_id     BIGINT,
    disbursement_status     VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- PENDING | POSTED | FAILED | SKIPPED
    disbursed_at            DATETIME,
    failure_reason          TEXT,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dividend_allocation_cycle FOREIGN KEY (cycle_id) REFERENCES dividend_cycle(id),
    UNIQUE KEY uq_cycle_member (cycle_id, member_id)
);

CREATE TABLE dividend_certificate (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    allocation_id           BIGINT          NOT NULL UNIQUE,
    certificate_number      VARCHAR(50)     NOT NULL UNIQUE,
    issued_at               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_allocation FOREIGN KEY (allocation_id) REFERENCES dividend_allocation(id)
);

CREATE INDEX idx_cycle_year         ON dividend_cycle(fiscal_year);
CREATE INDEX idx_alloc_cycle        ON dividend_allocation(cycle_id);
CREATE INDEX idx_alloc_member       ON dividend_allocation(member_id);
CREATE INDEX idx_alloc_status       ON dividend_allocation(disbursement_status);
