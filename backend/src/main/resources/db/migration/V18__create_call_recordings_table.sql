CREATE TABLE call_recordings (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    appointment_id UUID,
    patient_phone VARCHAR(20) NOT NULL,
    call_start_time TIMESTAMP NOT NULL,
    call_end_time TIMESTAMP,
    duration_seconds INTEGER,
    audio_file_path VARCHAR(500) NOT NULL,
    transcript TEXT,
    transcription_status VARCHAR(50) DEFAULT 'pending',
    storage_size_bytes BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recordings_workspace ON call_recordings(workspace_id);
CREATE INDEX idx_recordings_appointment ON call_recordings(appointment_id);
