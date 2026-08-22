-- ============================================================================
-- V1__Initial_Schema.sql
-- Purpose: Base database setup for Croi AI Workforce Platform.
-- Enables required PostgreSQL extensions used across all future migrations:
--   - pgcrypto  : provides gen_random_uuid() for UUID primary keys
--   - vector    : pgvector extension for storing/searching document embeddings (RAG)
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS vector;
