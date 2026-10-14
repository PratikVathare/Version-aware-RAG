package com.enterprise.rag.service;

import com.enterprise.rag.dto.PolicyConflictDto;
import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.service.impl.ConflictDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictDetectionServiceImplTest {

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    private ConflictDetectionServiceImpl conflictDetectionService;

    @BeforeEach
    void setUp() {
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        conflictDetectionService = new ConflictDetectionServiceImpl(chatClientBuilder);
    }

    @Test
    @DisplayName("Should detect conflict from Groq LLM response and return Current Version, Older Version, Description, and Warning")
    void testDetectConflictSuccess() {
        SearchResultDto currentChunk = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Travel Policy")
                .versionNumber(2)
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .chunkText("Daily meal allowance is $80.")
                .build();

        SearchResultDto olderChunk = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Travel Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2023, 1, 1))
                .chunkText("Daily meal allowance is $50.")
                .build();

        String llmJson = """
                {
                  "conflictDetected": true,
                  "conflictDescription": "Version 1 specifies daily meal allowance of $50, whereas Version 2 specifies $80."
                }
                """;

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(llmJson);

        Optional<PolicyConflictDto> conflictOpt = conflictDetectionService.detectConflict(
                "meal allowance", List.of(currentChunk), List.of(olderChunk)
        );

        assertTrue(conflictOpt.isPresent());
        PolicyConflictDto conflict = conflictOpt.get();
        assertEquals("Travel Policy", conflict.getDocumentName());
        assertEquals("v2", conflict.getCurrentVersion());
        assertEquals("v1", conflict.getOlderVersion());
        assertEquals("Version 1 specifies daily meal allowance of $50, whereas Version 2 specifies $80.", conflict.getConflictDescription());
        assertNotNull(conflict.getWarning());
        assertTrue(conflict.getWarning().contains("Warning: Conflict detected between current version (v2) and older version (v1)"));
    }

    @Test
    @DisplayName("Should return empty when no conflict is detected by LLM")
    void testNoConflictDetected() {
        SearchResultDto currentChunk = SearchResultDto.builder()
                .documentName("Travel Policy")
                .versionNumber(2)
                .chunkText("Flights over 4 hours may be booked in premium economy.")
                .build();

        SearchResultDto olderChunk = SearchResultDto.builder()
                .documentName("Travel Policy")
                .versionNumber(1)
                .chunkText("Hotel check-in must be done using corporate card.")
                .build();

        String llmJson = """
                {
                  "conflictDetected": false,
                  "conflictDescription": null
                }
                """;

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(llmJson);

        Optional<PolicyConflictDto> conflictOpt = conflictDetectionService.detectConflict(
                "hotel check-in", List.of(currentChunk), List.of(olderChunk)
        );

        assertFalse(conflictOpt.isPresent());
    }

    @Test
    @DisplayName("Should return empty when older chunks list is empty")
    void testEmptyOlderChunks() {
        SearchResultDto currentChunk = SearchResultDto.builder()
                .documentName("Policy")
                .versionNumber(1)
                .chunkText("Some policy text")
                .build();

        Optional<PolicyConflictDto> conflictOpt = conflictDetectionService.detectConflict(
                "test", List.of(currentChunk), List.of()
        );

        assertFalse(conflictOpt.isPresent());
        verifyNoInteractions(chatClientBuilder);
    }
}
