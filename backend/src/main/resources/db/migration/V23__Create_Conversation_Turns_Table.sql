-- Per-utterance breakdown of a call (caller turn + agent turn), distinct from
-- call_recordings' own call-level `transcript` (the full-conversation summary).
--
-- Deliberately no `audio_base64` column: storing base64 audio as TEXT in a
-- hot, frequently-scanned table bloats row size (~33% over raw bytes), WAL,
-- and replication for no benefit here — the call's audio is already written
-- to a file by CallRecordingService and referenced via
-- call_recordings.audio_file_path. Add per-turn audio storage separately
-- (e.g. one file per turn) if it's ever actually needed.
CREATE TABLE conversation_turns (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_recording_id UUID NOT NULL REFERENCES call_recordings (id) ON DELETE CASCADE,
    speaker           VARCHAR(10) NOT NULL CHECK (speaker IN ('caller', 'agent')),
    message           TEXT NOT NULL,
    transcript        TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversation_turns_call_recording ON conversation_turns (call_recording_id);
