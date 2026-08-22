ALTER TABLE organizations
    ADD COLUMN system_prompt TEXT NOT NULL DEFAULT 'You are Croi, {workspace_name}''s AI support assistant. Use the provided documents to answer questions.',
    ADD COLUMN tone VARCHAR(32) NOT NULL DEFAULT 'PROFESSIONAL',
    ADD COLUMN embedding_model VARCHAR(128) NOT NULL DEFAULT 'nomic-embed-text',
    ADD COLUMN similarity_threshold DOUBLE PRECISION NOT NULL DEFAULT 0.3,
    ADD COLUMN max_chunks_in_prompt INTEGER NOT NULL DEFAULT 5,
    ADD COLUMN temperature DOUBLE PRECISION NOT NULL DEFAULT 0.7,
    ADD COLUMN max_tokens INTEGER NOT NULL DEFAULT 500;
