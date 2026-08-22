CREATE TABLE queue_status (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL UNIQUE,
    current_patient_count INTEGER DEFAULT 0,
    estimated_wait_minutes INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'normal',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_queue_workspace ON queue_status(workspace_id);
