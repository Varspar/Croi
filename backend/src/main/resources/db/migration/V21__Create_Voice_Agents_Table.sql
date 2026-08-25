CREATE TABLE voice_agents (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    phone_number   VARCHAR(20),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (phone_number)
);

CREATE INDEX idx_voice_agents_organization ON voice_agents (organization_id);
