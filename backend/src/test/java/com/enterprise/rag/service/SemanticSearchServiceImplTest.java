package com.enterprise.rag.service;

import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.repository.ChunkSimilarityProjection;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.impl.SemanticSearchServiceImpl;
import com.pgvector.PGvector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemanticSearchServiceImplTest {

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    private SemanticSearchServiceImpl semanticSearchService;

    @BeforeEach
    void setUp() {
        semanticSearchService = new SemanticSearchServiceImpl(documentChunkRepository, embeddingService);
    }

    @Test
    @DisplayName("Should generate embedding for question and retrieve top chunks via pgvector without calling LLM")
    void testSemanticSearch() {
        String question = "What is the password rotation policy?";
        PGvector queryVector = new PGvector(new float[]{0.05f, -0.02f, 0.15f});

        when(embeddingService.generateEmbedding(question)).thenReturn(queryVector);

        UUID chunkId = UUID.randomUUID();
        ChunkSimilarityProjection projection = mock(ChunkSimilarityProjection.class);
        when(projection.getId()).thenReturn(chunkId);
        when(projection.getDocumentName()).thenReturn("Security Policy");
        when(projection.getVersionNumber()).thenReturn(1);
        when(projection.getEffectiveDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(projection.getDepartment()).thenReturn("Engineering");
        when(projection.getChunkText()).thenReturn("All API keys and passwords must be rotated every 90 days.");
        when(projection.getSimilarity()).thenReturn(0.8845);
        when(projection.getCreatedAt()).thenReturn(OffsetDateTime.now());

        when(documentChunkRepository.searchSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of(projection));

        List<SearchResultDto> results = semanticSearchService.searchSimilarChunks(question, 10);

        assertNotNull(results);
        assertEquals(1, results.size());

        SearchResultDto hit = results.get(0);
        assertEquals(chunkId, hit.getId());
        assertEquals("Security Policy", hit.getDocumentName());
        assertEquals(1, hit.getVersionNumber());
        assertEquals("Engineering", hit.getDepartment());
        assertEquals(0.8845, hit.getSimilarityScore());
        assertTrue(hit.getChunkText().contains("rotated every 90 days"));

        verify(embeddingService, times(1)).generateEmbedding(question);
        verify(documentChunkRepository, times(1)).searchSimilarChunks(eq(queryVector.getValue()), eq(10));
    }
}
