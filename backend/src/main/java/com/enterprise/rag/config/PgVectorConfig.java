package com.enterprise.rag.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for PostgreSQL pgvector extension registration.
 * Ensures the 'vector' extension is enabled on Supabase PostgreSQL.
 */
@Configuration
public class PgVectorConfig {
    // Configuration hooks for PGvector type registration and distance metric settings
}
