-- ==============================================================================
-- Supabase Migration: 20261013000001_create_document_chunks.sql
-- ==============================================================================

-- 1. Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Create document_chunks table
CREATE TABLE IF NOT EXISTS document_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_name TEXT NOT NULL,
    version_number INTEGER NOT NULL,
    effective_date DATE NOT NULL,
    department TEXT NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(384),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. B-Tree Indexes for fast filtering
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_name
    ON document_chunks (document_name);

CREATE INDEX IF NOT EXISTS idx_document_chunks_version_number
    ON document_chunks (version_number);

CREATE INDEX IF NOT EXISTS idx_document_chunks_effective_date
    ON document_chunks (effective_date);

CREATE INDEX IF NOT EXISTS idx_document_chunks_department
    ON document_chunks (department);

CREATE INDEX IF NOT EXISTS idx_document_chunks_doc_version
    ON document_chunks (document_name, version_number);

-- 4. HNSW vector index for cosine distance search
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding_hnsw
    ON document_chunks USING hnsw (embedding vector_cosine_ops);
