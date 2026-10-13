package com.enterprise.rag.controller;

import com.enterprise.rag.dto.SearchRequest;
import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.service.SemanticSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/search", "/api/v1/search"})
@Tag(name = "Semantic Search", description = "Endpoints for pgvector semantic vector similarity retrieval")
public class SearchController {

    private final SemanticSearchService semanticSearchService;

    public SearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Perform semantic vector similarity search",
            description = "Takes a question, computes its 384-dimensional embedding, executes pgvector cosine similarity search on Supabase, and returns the top 10 matching chunks without LLM inference.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Top matching document chunks ordered by vector similarity",
                            content = @Content(schema = @Schema(implementation = SearchResultDto.class))
                    )
            }
    )
    public ResponseEntity<List<SearchResultDto>> search(
            @Valid @RequestBody SearchRequest request) {

        int topK = (request.getTopK() != null && request.getTopK() > 0) ? request.getTopK() : 10;
        List<SearchResultDto> results = semanticSearchService.searchSimilarChunks(request.getQuestion(), topK);

        return ResponseEntity.ok(results);
    }
}
