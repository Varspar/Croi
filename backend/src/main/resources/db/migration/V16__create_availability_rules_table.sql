CREATE TABLE availability_rules (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    appointment_duration INTEGER DEFAULT 30,
    buffer_time INTEGER DEFAULT 10,
    max_daily_capacity INTEGER DEFAULT 20,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_availability_workspace ON availability_rules(workspace_id);
