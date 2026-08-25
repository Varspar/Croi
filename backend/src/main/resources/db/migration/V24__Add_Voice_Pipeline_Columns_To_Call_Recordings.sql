-- Links a recording to the VoiceAgent that handled it (recordings could only
-- reference an appointment before; now a call can exist with no appointment
-- at all). Nullable: older/appointment-only recordings won't have an agent.
ALTER TABLE call_recordings
    ADD COLUMN agent_id UUID REFERENCES voice_agents (id) ON DELETE SET NULL,
    ADD COLUMN outcome VARCHAR(50),
    ADD COLUMN outcome_notes TEXT;

CREATE INDEX idx_call_recordings_agent ON call_recordings (agent_id);
