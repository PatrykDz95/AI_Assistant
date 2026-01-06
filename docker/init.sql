-- Enable pgvector extension for vector storage
-- pgvector adds support for vector data types and operations in PostgreSQL
-- This is needed for storing embeddings (numerical representations of text)
CREATE EXTENSION IF NOT EXISTS vector;

CREATE SCHEMA IF NOT EXISTS public;

-- Grant privileges
-- Allow postgres user and public role to create tables and use the schema
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Create vector_store table for Spring AI PgVectorStore
CREATE TABLE IF NOT EXISTS public.vector_store (
    -- UUID primary key, automatically generated for each row
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- The actual text content of the document
    content TEXT,

    -- JSON metadata (can store source, timestamp, tags, etc.)
--     metadata JSONB,

    -- Vector embedding - numerical representation of the content
    -- 768 dimensions is the output size of nomic-embed-text model
    -- Each dimension is a float number representing a semantic feature
    embedding vector(768)
);

-- Create index for faster vector similarity search
-- ivfflat = Inverted File with Flat Compression (approximate nearest neighbor search)
-- vector_cosine_ops = Use cosine similarity for comparing vectors
-- This makes "find similar documents" queries much faster (milliseconds instead of seconds)
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON public.vector_store
    USING ivfflat (embedding vector_cosine_ops);


