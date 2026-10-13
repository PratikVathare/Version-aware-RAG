package com.enterprise.rag.service;

import com.enterprise.rag.dto.SearchResultDto;

import java.util.List;

public interface SemanticSearchService {

    /**
     * Performs semantic vector similarity search using pgvector.
     * Generates embedding for question, searches Supabase, and returns top K chunks.
     *
     * @param question the search question
     * @param topK number of chunks to return (default 10)
     * @return top K matching document chunks with similarity scores
     */
    List<SearchResultDto> searchSimilarChunks(String question, int topK);
}
