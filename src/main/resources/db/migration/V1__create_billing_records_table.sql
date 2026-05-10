CREATE TABLE billing_records (
    id BIGSERIAL PRIMARY KEY,
    data_subject_id VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billing_data_subject_id ON billing_records (data_subject_id);
CREATE INDEX idx_billing_status ON billing_records (status);
