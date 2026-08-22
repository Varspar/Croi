CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    patient_phone VARCHAR(20) NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    duration_minutes INTEGER DEFAULT 30,
    reason VARCHAR(255),
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    calendar_event_id VARCHAR(255),
    call_recording_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_workspace_date ON appointments(workspace_id, appointment_date);
CREATE INDEX idx_appointments_patient_phone ON appointments(workspace_id, patient_phone);
