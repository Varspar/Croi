CREATE TABLE voice_agent_configs (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id              UUID NOT NULL UNIQUE REFERENCES voice_agents (id) ON DELETE CASCADE,
    system_prompt         TEXT NOT NULL DEFAULT 'You are a helpful voice receptionist.',
    tone                  VARCHAR(32) NOT NULL DEFAULT 'PROFESSIONAL',
    temperature           DOUBLE PRECISION NOT NULL DEFAULT 0.7,
    model                 VARCHAR(128) NOT NULL DEFAULT 'anthropic/claude-3-haiku',
    max_duration_seconds  INTEGER NOT NULL DEFAULT 300,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_voice_agent_configs_agent ON voice_agent_configs (agent_id);
