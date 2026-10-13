package com.enterprise.rag.controller;

import com.enterprise.rag.dto.ChunkItemDto;
import com.enterprise.rag.dto.ChunkWithEmbeddingDto;
import com.enterprise.rag.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/embeddings", "/api/v1/embeddings"})
@Tag(name = "Embedding Generation", description = "Endpoints for generating sentence-transformers (all-MiniLM-L6-v2) vector embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    public EmbeddingController(EmbeddingService embeddingService, ObjectMapper objectMapper) {
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Generate embeddings for a list of document chunks",
            description = "Calls the local Python embedding service (sentence-transformers all-MiniLM-L6-v2, 384 dimensions). Accepts a list of chunks and returns each chunk paired with its embedding vector without storing in the database.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Chunks with corresponding 384-dimensional embedding vectors",
                            content = @Content(schema = @Schema(implementation = ChunkWithEmbeddingDto.class))
                    )
            }
    )
    public ResponseEntity<List<ChunkWithEmbeddingDto>> generateEmbeddings(@RequestBody JsonNode requestNode) {
        List<ChunkItemDto> chunks = parseChunksFromRequest(requestNode);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No valid chunks provided in request payload");
        }

        List<ChunkWithEmbeddingDto> response = embeddingService.embedChunks(chunks);
        return ResponseEntity.ok(response);
    }

    /**
     * Flexibly parses either:
     * 1. A JSON array of chunk objects: [ { "chunkIndex": 0, "text": "..." }, ... ]
     * 2. A JSON array of plain strings: [ "chunk 1", "chunk 2" ]
     * 3. A wrapper JSON object: { "chunks": [ ... ] }
     */
    private List<ChunkItemDto> parseChunksFromRequest(JsonNode requestNode) {
        List<ChunkItemDto> list = new ArrayList<>();

        if (requestNode == null || requestNode.isNull()) {
            return list;
        }

        JsonNode itemsNode = requestNode;
        if (requestNode.isObject() && requestNode.has("chunks")) {
            itemsNode = requestNode.get("chunks");
        }

        if (itemsNode.isArray()) {
            int index = 0;
            for (JsonNode item : itemsNode) {
                if (item.isTextual()) {
                    list.add(ChunkItemDto.builder()
                            .chunkIndex(index++)
                            .text(item.asText())
                            .build());
                } else if (item.isObject()) {
                    ChunkItemDto dto = objectMapper.convertValue(item, ChunkItemDto.class);
                    if (dto.getChunkIndex() == null) {
                        dto.setChunkIndex(index);
                    }
                    index++;
                    list.add(dto);
                }
            }
        }

        return list;
    }
}
