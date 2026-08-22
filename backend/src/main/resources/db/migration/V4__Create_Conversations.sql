-- ============================================================================
-- V4__Create_Conversations.sql
-- Purpose: Stores chat conversations between customers and the AI employee
-- (e.g. Customer Support Specialist), and the individual messages within them.
-- ============================================================================

CREATE TABLE conversations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    customer_name   VARCHAR(255),
    customer_email  VARCHAR(255),
    status          VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    is_escalated    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversations_organization_id ON conversations (organization_id);
CREATE INDEX idx_conversations_status ON conversations (status);
CREATE INDEX idx_conversations_created_at ON conversations (created_at);

-- Individual messages within a conversation, from either the customer or the AI agent.
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_type     VARCHAR(50) NOT NULL, -- CUSTOMER, AI, AGENT (human escalation)
    content         TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX idx_messages_created_at ON messages (created_at);
