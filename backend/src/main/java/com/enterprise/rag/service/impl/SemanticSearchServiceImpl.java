package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.repository.ChunkSimilarityProjection;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.EmbeddingService;
import com.enterprise.rag.service.SemanticSearchService;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SemanticSearchServiceImpl implements SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public SemanticSearchServiceImpl(DocumentChunkRepository documentChunkRepository, EmbeddingService embeddingService) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResultDto> searchSimilarChunks(String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty for semantic search");
        }

        int limit = (topK > 0) ? topK : 10;
        log.info("Executing semantic search for question: '{}' (topK={})", question, limit);

        // 1. Generate 384-dimensional query embedding via local Python embedding service
        PGvector queryEmbedding = embeddingService.generateEmbedding(question);
        if (queryEmbedding == null) {
            log.warn("Embedding generation returned null for query: '{}'", question);
            return Collections.emptyList();
        }

        String embeddingString = queryEmbedding.getValue();

        // 2. Perform pgvector cosine similarity search in Supabase PostgreSQL
        List<ChunkSimilarityProjection> projections =
                documentChunkRepository.searchSimilarChunks(embeddingString, limit);

        log.info("Semantic search retrieved {} chunks from Supabase", projections.size());

        // 3. Map to SearchResultDto with similarity score (Without calling LLM)
        return projections.stream()
                .map(p -> SearchResultDto.builder()
                        .id(p.getId())
                        .documentName(p.getDocumentName())
                        .versionNumber(p.getVersionNumber())
                        .effectiveDate(p.getEffectiveDate())
                        .department(p.getDepartment())
                        .chunkText(p.getChunkText())
                        .similarityScore(p.getSimilarity() != null ? Math.round(p.getSimilarity() * 10000.0) / 10000.0 : null)
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
    }
}
