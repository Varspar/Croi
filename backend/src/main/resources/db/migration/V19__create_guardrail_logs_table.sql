CREATE TABLE guardrail_logs (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    detected_request TEXT NOT NULL,
    business_type VARCHAR(100),
    was_out_of_scope BOOLEAN,
    required_alternative VARCHAR(100),
    is_emergency BOOLEAN,
    patient_phone VARCHAR(20),
    call_recording_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_guardrail_workspace ON guardrail_logs(workspace_id);
CREATE INDEX idx_guardrail_emergency ON guardrail_logs(is_emergency);
