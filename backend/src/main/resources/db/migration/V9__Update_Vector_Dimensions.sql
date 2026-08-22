-- Ollama's nomic-embed-text model produces 768-dimensional vectors.
-- Existing vectors belong to the previous, incompatible provider and must be regenerated.
DROP INDEX IF EXISTS idx_document_embeddings_vector;
DELETE FROM document_embeddings;
ALTER TABLE document_embeddings ALTER COLUMN embedding TYPE vector(768);
CREATE INDEX idx_document_embeddings_vector ON document_embeddings
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
