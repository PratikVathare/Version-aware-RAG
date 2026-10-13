package com.enterprise.rag.service;

import com.enterprise.rag.dto.ChunkItemDto;
import com.enterprise.rag.dto.ChunkWithEmbeddingDto;
import com.enterprise.rag.dto.PythonEmbedRequest;
import com.enterprise.rag.dto.PythonEmbedResponse;
import com.enterprise.rag.service.impl.EmbeddingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmbeddingServiceImpl embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingServiceImpl(restClient);
        ReflectionTestUtils.setField(embeddingService, "embeddingDimension", 384);
        ReflectionTestUtils.setField(embeddingService, "modelName", "sentence-transformers/all-MiniLM-L6-v2");
    }

    @Test
    @DisplayName("Should embed chunks by delegating to local Python service and mapping vectors")
    void testEmbedChunks() {
        List<ChunkItemDto> chunks = List.of(
                ChunkItemDto.builder().chunkIndex(0).text("First chunk text").build(),
                ChunkItemDto.builder().chunkIndex(1).text("Second chunk text").build()
        );

        List<Float> vector1 = List.of(0.1f, 0.2f, 0.3f);
        List<Float> vector2 = List.of(0.4f, 0.5f, 0.6f);

        PythonEmbedResponse mockPythonResponse = PythonEmbedResponse.builder()
                .model("sentence-transformers/all-MiniLM-L6-v2")
                .dimension(3)
                .embeddings(List.of(vector1, vector2))
                .build();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PythonEmbedRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PythonEmbedResponse.class)).thenReturn(mockPythonResponse);

        List<ChunkWithEmbeddingDto> result = embeddingService.embedChunks(chunks);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(0, result.get(0).getChunkIndex());
        assertEquals("First chunk text", result.get(0).getText());
        assertEquals(vector1, result.get(0).getEmbedding());

        assertEquals(1, result.get(1).getChunkIndex());
        assertEquals("Second chunk text", result.get(1).getText());
        assertEquals(vector2, result.get(1).getEmbedding());
    }
}
