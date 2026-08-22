-- ============================================================================
-- V5__Create_Knowledge_Base.sql
-- Purpose: Stores uploaded company documentation (documents) and their
-- vector embeddings (document_embeddings) used for Retrieval-Augmented
-- Generation (RAG) when the AI answers customer questions.
-- ============================================================================

CREATE TABLE documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING, READY, FAILED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_organization_id ON documents (organization_id);
CREATE INDEX idx_documents_status ON documents (status);

-- Chunked embeddings for each document, used for similarity search via pgvector.
-- Vector dimension of 1536 matches OpenAI's text-embedding-3-small model.
CREATE TABLE document_embeddings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    chunk_index     INT NOT NULL,
    content         TEXT NOT NULL,
    embedding       vector(1536) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_document_embeddings_document_id ON document_embeddings (document_id);

-- IVFFlat index for approximate nearest-neighbor vector similarity search.
CREATE INDEX idx_document_embeddings_vector ON document_embeddings
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
