package com.enterprise.rag.service;

import com.enterprise.rag.dto.PolicyConflictDto;
import com.enterprise.rag.dto.RagQueryRequest;
import com.enterprise.rag.dto.RagQueryResponse;
import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.impl.RagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagServiceImplTest {

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private ConflictDetectionService conflictDetectionService;

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    private RagServiceImpl ragService;

    @BeforeEach
    void setUp() {
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        ragService = new RagServiceImpl(
                documentChunkRepository,
                embeddingService,
                semanticSearchService,
                chatClientBuilder,
                conflictDetectionService
        );
    }

    @Test
    @DisplayName("Should detect conflict between versions and return Current Version, Older Version, Conflict Description, and Warning")
    void testGroqIntegrationAndConflictDetection() {
        String query = "What is the password expiry rule?";
        RagQueryRequest request = RagQueryRequest.builder()
                .query(query)
                .topK(5)
                .build();

        // Older version 1 chunk
        SearchResultDto v1Chunk = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Security Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2023, 1, 1))
                .department("IT")
                .chunkText("Passwords expire every 30 days.")
                .similarityScore(0.95)
                .build();

        // Current version 2 chunk
        SearchResultDto v2Chunk = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Security Policy")
                .versionNumber(2)
                .effectiveDate(LocalDate.of(2024, 1, 15))
                .department("IT")
                .chunkText("Passwords expire every 90 days with multi-factor authentication.")
                .similarityScore(0.88)
                .build();

        when(semanticSearchService.searchRawSimilarChunks(eq(query), anyInt()))
                .thenReturn(List.of(v1Chunk, v2Chunk));

        PolicyConflictDto conflict = PolicyConflictDto.builder()
                .documentName("Security Policy")
                .currentVersion("v2")
                .olderVersion("v1")
                .conflictDescription("Version 1 specifies password expiry every 30 days, whereas current Version 2 specifies 90 days with MFA.")
                .warning("Warning: Conflict detected between current version (v2) and older version (v1) of 'Security Policy'. Version 1 specifies password expiry every 30 days, whereas current Version 2 specifies 90 days with MFA. Answer was generated using the current version.")
                .build();

        when(conflictDetectionService.detectConflict(eq(query), anyList(), anyList()))
                .thenReturn(Optional.of(conflict));

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        when(requestSpec.system(systemPromptCaptor.capture())).thenReturn(requestSpec);

        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        when(requestSpec.user(userPromptCaptor.capture())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("According to Security Policy v2 effective 2024-01-15, passwords expire every 90 days with MFA.");

        RagQueryResponse response = ragService.executeVersionAwareQuery(request);

        // Verify response includes Answer, Document Name, Version Number, and Effective Date
        assertNotNull(response);
        assertEquals("According to Security Policy v2 effective 2024-01-15, passwords expire every 90 days with MFA.", response.getAnswer());
        assertEquals("Security Policy", response.getDocumentName());
        assertEquals(2, response.getVersionNumber());
        assertEquals(LocalDate.of(2024, 1, 15), response.getEffectiveDate());

        // Verify Conflict Detection returns: Current Version, Older Version, Conflict Description, Warning
        assertEquals("v2", response.getCurrentVersion());
        assertEquals("v1", response.getOlderVersion());
        assertEquals("Version 1 specifies password expiry every 30 days, whereas current Version 2 specifies 90 days with MFA.", response.getConflictDescription());
        assertNotNull(response.getWarning());
        assertTrue(response.getWarning().contains("Warning: Conflict detected"));
        assertNotNull(response.getConflict());

        // Verify prompt templates were used with ONLY current version context
        String renderedUserPrompt = userPromptCaptor.getValue();
        assertTrue(renderedUserPrompt.contains("Passwords expire every 90 days with multi-factor authentication."));
        assertFalse(renderedUserPrompt.contains("Passwords expire every 30 days."));
    }

    @Test
    @DisplayName("Should never allow model to answer without retrieved context when no chunks are found")
    void testNeverAllowModelToAnswerWithoutRetrievedContext() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What is the secret cafeteria discount?")
                .build();

        when(semanticSearchService.searchRawSimilarChunks(anyString(), anyInt()))
                .thenReturn(List.of());

        RagQueryResponse response = ragService.executeVersionAwareQuery(request);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("no relevant context was found"));
        assertNull(response.getDocumentName());
        assertNull(response.getVersionNumber());
        assertNull(response.getEffectiveDate());
        assertNull(response.getWarning());
        assertTrue(response.getSourceChunks().isEmpty());

        // CRITICAL: ChatClient / LLM is NEVER called when there is no retrieved context
        verifyNoInteractions(chatClientBuilder);
        verifyNoInteractions(conflictDetectionService);
    }
}
