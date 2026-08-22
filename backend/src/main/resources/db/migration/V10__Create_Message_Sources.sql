CREATE TABLE message_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    chunk_id UUID NOT NULL REFERENCES document_embeddings (id) ON DELETE CASCADE,
    relevance_score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_message_sources_message_id ON message_sources (message_id);
CREATE INDEX idx_message_sources_document_id ON message_sources (document_id);
