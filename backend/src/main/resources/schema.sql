-- ==============================================================================
-- Supabase PostgreSQL Schema Migration: Version-Aware Document Chunks
-- Vector Dimension: 384 (sentence-transformers/all-MiniLM-L6-v2)
-- ==============================================================================

-- 1. Enable pgvector and UUID extensions
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

-- 3. Metadata Indexes for Version-Aware & Department-Aware Queries
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_name
    ON document_chunks (document_name);

CREATE INDEX IF NOT EXISTS idx_document_chunks_version_number
    ON document_chunks (version_number);

CREATE INDEX IF NOT EXISTS idx_document_chunks_effective_date
    ON document_chunks (effective_date);

CREATE INDEX IF NOT EXISTS idx_document_chunks_department
    ON document_chunks (department);

-- Composite index for fast version lookups per document
CREATE INDEX IF NOT EXISTS idx_document_chunks_doc_version
    ON document_chunks (document_name, version_number);

-- 4. pgvector Approximate Nearest Neighbor (ANN) Index
-- HNSW (Hierarchical Navigable Small World) index using cosine distance (vector_cosine_ops)
-- Recommended for Supabase & all-MiniLM-L6-v2 384-dimensional embeddings
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding_hnsw
    ON document_chunks USING hnsw (embedding vector_cosine_ops);

-- ==============================================================================
-- Optional: Supabase RPC Function for Version-Aware Similarity Search
-- ==============================================================================
CREATE OR REPLACE FUNCTION match_document_chunks (
    query_embedding VECTOR(384),
    match_threshold FLOAT,
    match_count INT,
    filter_document_name TEXT DEFAULT NULL,
    filter_version_number INT DEFAULT NULL,
    filter_department TEXT DEFAULT NULL,
    filter_effective_date DATE DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    document_name TEXT,
    version_number INT,
    effective_date DATE,
    department TEXT,
    chunk_text TEXT,
    similarity FLOAT,
    created_at TIMESTAMP WITH TIME ZONE
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        dc.id,
        dc.document_name,
        dc.version_number,
        dc.effective_date,
        dc.department,
        dc.chunk_text,
        1 - (dc.embedding <=> query_embedding) AS similarity,
        dc.created_at
    FROM document_chunks dc
    WHERE (filter_document_name IS NULL OR dc.document_name = filter_document_name)
      AND (filter_version_number IS NULL OR dc.version_number = filter_version_number)
      AND (filter_department IS NULL OR dc.department = filter_department)
      AND (filter_effective_date IS NULL OR dc.effective_date <= filter_effective_date)
      AND (1 - (dc.embedding <=> query_embedding)) >= match_threshold
    ORDER BY dc.embedding <=> query_embedding
    LIMIT match_count;
END;
$$;
