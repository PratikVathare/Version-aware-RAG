package com.enterprise.rag.service;

import com.enterprise.rag.dto.SearchResultDto;

import java.util.List;

public interface SemanticSearchService {

    /**
     * Performs semantic vector similarity search using pgvector.
     * Generates embedding for question, searches Supabase, and returns top K chunks
     * with version filtering applied (retaining only current version chunks).
     *
     * @param question the search question
     * @param topK number of chunks to return (default 10)
     * @return top K matching current-version document chunks with similarity scores
     */
    List<SearchResultDto> searchSimilarChunks(String question, int topK);

    /**
     * Performs raw semantic vector similarity search using pgvector without version filtering.
     * Returns all matching chunks including older versions.
     *
     * @param question the search question
     * @param topK number of chunks to return
     * @return all matching document chunks with similarity scores
     */
    List<SearchResultDto> searchRawSimilarChunks(String question, int topK);
}
