-- This is needed for storing embeddings (numerical representations of text)
CREATE EXTENSION IF NOT EXISTS vector;

-- Ensure public schema exists
CREATE SCHEMA IF NOT EXISTS public;

-- Grant privileges
-- Allow postgres user and public role to create tables and use the schema
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

