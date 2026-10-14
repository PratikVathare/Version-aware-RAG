package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.DocumentChunkDto;
import com.enterprise.rag.dto.PolicyConflictDto;
import com.enterprise.rag.dto.RagQueryRequest;
import com.enterprise.rag.dto.RagQueryResponse;
import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.ConflictDetectionService;
import com.enterprise.rag.service.EmbeddingService;
import com.enterprise.rag.service.RagService;
import com.enterprise.rag.service.SemanticSearchService;
import com.enterprise.rag.util.VersionFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    public static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an enterprise AI assistant for Version-Aware RAG powered by Groq.
            Your purpose is to answer user questions using exclusively the provided document context from current versions.
            
            CRITICAL CONSTRAINTS:
            1. You must NEVER answer without retrieved context or using prior/external knowledge.
            2. If the retrieved context is missing, empty, or insufficient to answer the question, you must strictly state:
               "I cannot answer this question because no relevant context was found in the enterprise documents."
            3. In your response, explicitly reference the Document Name, Version Number, and Effective Date from the provided context.
            """;

    public static final String USER_PROMPT_TEMPLATE = """
            Retrieved Context from Current Document Versions:
            ---------------------
            {context}
            ---------------------
            
            User Question: {question}
            
            Instructions:
            Using ONLY the retrieved context above, provide a factual answer.
            Explicitly cite the Document Name, Version Number, and Effective Date in your response.
            """;

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final SemanticSearchService semanticSearchService;
    private final ChatClient.Builder chatClientBuilder;
    private final ConflictDetectionService conflictDetectionService;

    public RagServiceImpl(DocumentChunkRepository documentChunkRepository,
                          EmbeddingService embeddingService,
                          SemanticSearchService semanticSearchService,
                          ChatClient.Builder chatClientBuilder,
                          ConflictDetectionService conflictDetectionService) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
        this.semanticSearchService = semanticSearchService;
        this.chatClientBuilder = chatClientBuilder;
        this.conflictDetectionService = conflictDetectionService;
    }

    @Override
    public RagQueryResponse executeVersionAwareQuery(RagQueryRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty for version-aware RAG");
        }

        long startTime = System.currentTimeMillis();
        int topK = (request.getTopK() != null && request.getTopK() > 0) ? request.getTopK() : 5;

        // 1. Semantic retrieval (retrieves raw candidate chunks including multiple versions)
        List<SearchResultDto> rawChunks = semanticSearchService.searchRawSimilarChunks(request.getQuery(), topK * 2);

        // 2. Apply request-specific filters if provided
        List<SearchResultDto> filteredByRequest = rawChunks.stream()
                .filter(chunk -> request.getDocumentName() == null || request.getDocumentName().isBlank()
                        || request.getDocumentName().equalsIgnoreCase(chunk.getDocumentName()))
                .filter(chunk -> request.getDepartment() == null || request.getDepartment().isBlank()
                        || request.getDepartment().equalsIgnoreCase(chunk.getDepartment()))
                .filter(chunk -> request.getVersionNumber() == null
                        || request.getVersionNumber().equals(chunk.getVersionNumber()))
                .filter(chunk -> request.getAsOfDate() == null || chunk.getEffectiveDate() == null
                        || !chunk.getEffectiveDate().isAfter(request.getAsOfDate()))
                .toList();

        // 3. Version partitioning: Separate current version chunks from older version chunks
        VersionFilterUtils.VersionPartitionResult<SearchResultDto> partition =
                VersionFilterUtils.partitionByVersion(
                        filteredByRequest,
                        SearchResultDto::getDocumentName,
                        SearchResultDto::getVersionNumber,
                        SearchResultDto::getEffectiveDate
                );

        List<SearchResultDto> currentVersionChunks = partition.currentVersionChunks();
        List<SearchResultDto> olderVersionChunks = partition.olderVersionChunks();

        // 4. GUARD: Never allow the model to answer without retrieved context
        if (currentVersionChunks.isEmpty()) {
            log.warn("No retrieved context available for query: '{}'. Model is never allowed to answer without retrieved context.",
                    request.getQuery());
            return RagQueryResponse.builder()
                    .answer("I cannot answer this question because no relevant context was found in the enterprise documents.")
                    .documentName(null)
                    .versionNumber(null)
                    .effectiveDate(null)
                    .query(request.getQuery())
                    .resolvedVersion(null)
                    .sourceChunks(Collections.emptyList())
                    .latencyMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        // 5. Conflict Detection: If retrieved chunks from different versions contain contradictory values, detect conflict
        Optional<PolicyConflictDto> conflictOpt = Optional.empty();
        if (!olderVersionChunks.isEmpty()) {
            conflictOpt = conflictDetectionService.detectConflict(request.getQuery(), currentVersionChunks, olderVersionChunks);
        }

        String warning = conflictOpt.map(PolicyConflictDto::getWarning).orElse(null);
        String currentVersionVal = conflictOpt.map(PolicyConflictDto::getCurrentVersion).orElse(null);
        String olderVersionVal = conflictOpt.map(PolicyConflictDto::getOlderVersion).orElse(null);
        String conflictDescVal = conflictOpt.map(PolicyConflictDto::getConflictDescription).orElse(null);

        // Limit current version chunks to topK
        if (currentVersionChunks.size() > topK) {
            currentVersionChunks = currentVersionChunks.subList(0, topK);
        }

        // 6. Send the retrieved chunks as context using structured context formatting
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < currentVersionChunks.size(); i++) {
            SearchResultDto chunk = currentVersionChunks.get(i);
            contextBuilder.append(String.format("""
                    [Chunk %d]
                    Document Name: %s
                    Version Number: %s
                    Effective Date: %s
                    Department: %s
                    Content:
                    %s
                    
                    """,
                    i + 1,
                    chunk.getDocumentName() != null ? chunk.getDocumentName() : "Unknown Document",
                    chunk.getVersionNumber() != null ? "v" + chunk.getVersionNumber() : "N/A",
                    chunk.getEffectiveDate() != null ? chunk.getEffectiveDate().toString() : "N/A",
                    chunk.getDepartment() != null ? chunk.getDepartment() : "General",
                    chunk.getChunkText()));
        }

        // 7. Use prompt templates for System and User prompts
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT_TEMPLATE);
        String renderedSystemPrompt = systemPromptTemplate.render();

        PromptTemplate userPromptTemplate = new PromptTemplate(USER_PROMPT_TEMPLATE);
        String renderedUserPrompt = userPromptTemplate.render(Map.of(
                "context", contextBuilder.toString().trim(),
                "question", request.getQuery()
        ));

        // 8. Invoke Groq LLM inference via ChatClient
        ChatClient chatClient = chatClientBuilder.build();
        String answer = chatClient.prompt()
                .system(renderedSystemPrompt)
                .user(renderedUserPrompt)
                .call()
                .content();

        // 9. Extract primary Document Name, Version Number, and Effective Date for the response
        SearchResultDto primaryChunk = currentVersionChunks.get(0);
        List<String> distinctDocNames = currentVersionChunks.stream()
                .map(SearchResultDto::getDocumentName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        String responseDocName = distinctDocNames.size() > 1
                ? String.join(", ", distinctDocNames)
                : primaryChunk.getDocumentName();
        Integer responseVersion = primaryChunk.getVersionNumber();
        LocalDate responseDate = primaryChunk.getEffectiveDate();

        if (currentVersionVal == null && responseVersion != null) {
            currentVersionVal = "v" + responseVersion;
        }

        // 10. Map to source chunks DTO
        List<DocumentChunkDto> sourceChunks = currentVersionChunks.stream()
                .map(c -> DocumentChunkDto.builder()
                        .id(c.getId())
                        .documentName(c.getDocumentName())
                        .versionNumber(c.getVersionNumber())
                        .effectiveDate(c.getEffectiveDate())
                        .department(c.getDepartment())
                        .chunkText(c.getChunkText())
                        .score(c.getSimilarityScore())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        String resolvedVersion = currentVersionChunks.stream()
                .map(c -> c.getDocumentName() + " (v" + (c.getVersionNumber() != null ? c.getVersionNumber() : "latest") + ")")
                .distinct()
                .collect(Collectors.joining(", "));

        return RagQueryResponse.builder()
                .answer(answer)
                .documentName(responseDocName)
                .versionNumber(responseVersion)
                .effectiveDate(responseDate)
                .currentVersion(currentVersionVal)
                .olderVersion(olderVersionVal)
                .conflictDescription(conflictDescVal)
                .warning(warning)
                .conflict(conflictOpt.orElse(null))
                .query(request.getQuery())
                .resolvedVersion(resolvedVersion)
                .sourceChunks(sourceChunks)
                .latencyMs(System.currentTimeMillis() - startTime)
                .build();
    }
}
