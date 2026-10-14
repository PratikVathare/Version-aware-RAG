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
        when(projection.getCreatedAt()).thenReturn(java.time.Instant.now());

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

    @Test
    @DisplayName("Should filter out older versions and keep only latest version when multiple versions are retrieved")
    void testSemanticSearchFiltersOlderVersions() {
        String question = "What is the password rotation policy?";
        PGvector queryVector = new PGvector(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingService.generateEmbedding(question)).thenReturn(queryVector);

        // Version 1 of Security Policy (older version, higher similarity score)
        ChunkSimilarityProjection v1 = mock(ChunkSimilarityProjection.class);
        when(v1.getId()).thenReturn(UUID.randomUUID());
        when(v1.getDocumentName()).thenReturn("Security Policy");
        when(v1.getVersionNumber()).thenReturn(1);
        when(v1.getEffectiveDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(v1.getDepartment()).thenReturn("IT");
        when(v1.getChunkText()).thenReturn("Passwords expire every 30 days.");
        when(v1.getSimilarity()).thenReturn(0.95);
        when(v1.getCreatedAt()).thenReturn(java.time.Instant.now());

        // Version 2 of Security Policy (latest version, lower similarity score)
        ChunkSimilarityProjection v2 = mock(ChunkSimilarityProjection.class);
        when(v2.getId()).thenReturn(UUID.randomUUID());
        when(v2.getDocumentName()).thenReturn("Security Policy");
        when(v2.getVersionNumber()).thenReturn(2);
        when(v2.getEffectiveDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(v2.getDepartment()).thenReturn("IT");
        when(v2.getChunkText()).thenReturn("Passwords expire every 90 days with MFA.");
        when(v2.getSimilarity()).thenReturn(0.89);
        when(v2.getCreatedAt()).thenReturn(java.time.Instant.now());

        when(documentChunkRepository.searchSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of(v1, v2));

        List<SearchResultDto> results = semanticSearchService.searchSimilarChunks(question, 10);

        // Only the current version (v2) chunks must be returned; older version (v1) ignored
        assertNotNull(results);
        assertEquals(1, results.size());
        SearchResultDto retainedChunk = results.get(0);
        assertEquals(2, retainedChunk.getVersionNumber());
        assertEquals("Passwords expire every 90 days with MFA.", retainedChunk.getChunkText());
    }

    @Test
    @DisplayName("Should resolve latest version using most recent effective date when version numbers are equal")
    void testSemanticSearchFiltersByEffectiveDateWhenVersionNumbersEqual() {
        String question = "What is the travel per diem allowance?";
        PGvector queryVector = new PGvector(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingService.generateEmbedding(question)).thenReturn(queryVector);

        // 2023 version with versionNumber = 1
        ChunkSimilarityProjection oldDateChunk = mock(ChunkSimilarityProjection.class);
        when(oldDateChunk.getId()).thenReturn(UUID.randomUUID());
        when(oldDateChunk.getDocumentName()).thenReturn("Travel Policy");
        when(oldDateChunk.getVersionNumber()).thenReturn(1);
        when(oldDateChunk.getEffectiveDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(oldDateChunk.getChunkText()).thenReturn("Per diem is $50/day");
        when(oldDateChunk.getSimilarity()).thenReturn(0.92);

        // 2024 version with versionNumber = 1
        ChunkSimilarityProjection newDateChunk = mock(ChunkSimilarityProjection.class);
        when(newDateChunk.getId()).thenReturn(UUID.randomUUID());
        when(newDateChunk.getDocumentName()).thenReturn("Travel Policy");
        when(newDateChunk.getVersionNumber()).thenReturn(1);
        when(newDateChunk.getEffectiveDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(newDateChunk.getChunkText()).thenReturn("Per diem is $80/day");
        when(newDateChunk.getSimilarity()).thenReturn(0.87);

        when(documentChunkRepository.searchSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of(oldDateChunk, newDateChunk));

        List<SearchResultDto> results = semanticSearchService.searchSimilarChunks(question, 10);

        // Only 2024 chunk kept based on most recent effective date
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(LocalDate.of(2024, 1, 1), results.get(0).getEffectiveDate());
        assertEquals("Per diem is $80/day", results.get(0).getChunkText());
    }

    @Test
    @DisplayName("Should return raw chunks including multiple versions without filtering")
    void testSearchRawSimilarChunks() {
        String question = "Security";
        PGvector queryVector = new PGvector(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingService.generateEmbedding(question)).thenReturn(queryVector);

        ChunkSimilarityProjection p1 = mock(ChunkSimilarityProjection.class);
        when(p1.getVersionNumber()).thenReturn(1);
        ChunkSimilarityProjection p2 = mock(ChunkSimilarityProjection.class);
        when(p2.getVersionNumber()).thenReturn(2);

        when(documentChunkRepository.searchSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of(p1, p2));

        List<SearchResultDto> results = semanticSearchService.searchRawSimilarChunks(question, 10);
        assertEquals(2, results.size());
    }
}
