-- V15-V19 created the appointment-system tables without foreign keys, so a
-- deleted organization would leave orphaned appointments/availability rules/
-- queue rows/recordings/guardrail logs behind instead of cascading. This adds
-- the referential integrity every other tenant-scoped table in this schema has.

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_workspace FOREIGN KEY (workspace_id) REFERENCES organizations (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_appointments_call_recording FOREIGN KEY (call_recording_id) REFERENCES call_recordings (id) ON DELETE SET NULL;

ALTER TABLE availability_rules
    ADD CONSTRAINT fk_availability_rules_workspace FOREIGN KEY (workspace_id) REFERENCES organizations (id) ON DELETE CASCADE;

ALTER TABLE queue_status
    ADD CONSTRAINT fk_queue_status_workspace FOREIGN KEY (workspace_id) REFERENCES organizations (id) ON DELETE CASCADE;

ALTER TABLE call_recordings
    ADD CONSTRAINT fk_call_recordings_workspace FOREIGN KEY (workspace_id) REFERENCES organizations (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_call_recordings_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE SET NULL;

ALTER TABLE guardrail_logs
    ADD CONSTRAINT fk_guardrail_logs_workspace FOREIGN KEY (workspace_id) REFERENCES organizations (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_guardrail_logs_call_recording FOREIGN KEY (call_recording_id) REFERENCES call_recordings (id) ON DELETE SET NULL;
