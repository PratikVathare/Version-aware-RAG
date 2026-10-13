package com.enterprise.rag.service;

import com.enterprise.rag.dto.DocumentChunkDto;
import com.enterprise.rag.dto.DocumentUploadRequest;
import com.enterprise.rag.entity.DocumentChunk;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.impl.DocumentServiceImpl;
import com.pgvector.PGvector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private PdfDocumentService pdfDocumentService;

    @Captor
    private ArgumentCaptor<List<DocumentChunk>> entitiesCaptor;

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(documentChunkRepository, embeddingService, pdfDocumentService);
    }

    @Test
    @DisplayName("Should chunk text, generate embeddings, and store every chunk in Supabase via JPA")
    void testIngestDocumentAndStoreInSupabase() {
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .documentName("Security Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2024, 1, 15))
                .department("Information Security")
                .content("This is section 1 of security policy. It governs password policies and access control protocols across engineering.")
                .build();

        PGvector dummyEmbedding = new PGvector(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingService.generateBatchEmbeddings(anyList())).thenReturn(List.of(dummyEmbedding));

        when(documentChunkRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<DocumentChunk> input = invocation.getArgument(0);
            return input.stream()
                    .map(c -> DocumentChunk.builder()
                            .id(UUID.randomUUID())
                            .documentName(c.getDocumentName())
                            .versionNumber(c.getVersionNumber())
                            .effectiveDate(c.getEffectiveDate())
                            .department(c.getDepartment())
                            .chunkText(c.getChunkText())
                            .embedding(c.getEmbedding())
                            .createdAt(OffsetDateTime.now())
                            .build())
                    .toList();
        });

        List<DocumentChunkDto> result = documentService.ingestDocument(request);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(documentChunkRepository).saveAll(entitiesCaptor.capture());
        List<DocumentChunk> captured = entitiesCaptor.getValue();
        assertEquals(1, captured.size());

        DocumentChunk savedChunk = captured.get(0);
        assertEquals("Security Policy", savedChunk.getDocumentName());
        assertEquals(1, savedChunk.getVersionNumber());
        assertEquals(LocalDate.of(2024, 1, 15), savedChunk.getEffectiveDate());
        assertEquals("Information Security", savedChunk.getDepartment());
        assertNotNull(savedChunk.getChunkText());
        assertEquals(dummyEmbedding, savedChunk.getEmbedding());
        assertNotNull(savedChunk.getCreatedAt());
    }
}
