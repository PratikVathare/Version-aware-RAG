package com.enterprise.rag.controller;

import com.enterprise.rag.dto.ApiResponse;
import com.enterprise.rag.dto.RagQueryRequest;
import com.enterprise.rag.dto.RagQueryResponse;
import com.enterprise.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "Version-Aware RAG Query", description = "Query endpoint incorporating version resolution and Groq LLM generation")
public class RagQueryController {

    private final RagService ragService;

    public RagQueryController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/query")
    @Operation(summary = "Perform a version-aware RAG query", description = "Executes hybrid vector similarity search and LLM response generation with version constraints")
    public ResponseEntity<ApiResponse<RagQueryResponse>> query(
            @Valid @RequestBody RagQueryRequest request) {
        RagQueryResponse response = ragService.executeVersionAwareQuery(request);
        return ResponseEntity.ok(ApiResponse.ok("Query executed successfully", response));
    }
}
