package com.enterprise.rag.service;

import com.enterprise.rag.dto.PolicyConflictDto;
import com.enterprise.rag.dto.SearchResultDto;

import java.util.List;
import java.util.Optional;

public interface ConflictDetectionService {

    /**
     * Detects whether retrieved chunks from different versions contain contradictory values
     * for the same policy.
     *
     * @param query the policy query or question
     * @param currentVersionChunks current version chunks
     * @param olderVersionChunks older version chunks
     * @return Optional containing PolicyConflictDto if contradiction detected, empty otherwise
     */
    Optional<PolicyConflictDto> detectConflict(
            String query,
            List<SearchResultDto> currentVersionChunks,
            List<SearchResultDto> olderVersionChunks
    );
}
