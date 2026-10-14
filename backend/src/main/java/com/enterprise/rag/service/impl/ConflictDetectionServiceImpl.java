package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.PolicyConflictDto;
import com.enterprise.rag.dto.SearchResultDto;
import com.enterprise.rag.service.ConflictDetectionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    private static final Logger log = LoggerFactory.getLogger(ConflictDetectionServiceImpl.class);

    public static final String CONFLICT_SYSTEM_TEMPLATE = """
            You are an enterprise document conflict detection agent.
            Your role is to analyze retrieved text chunks from two different versions of the same document: a CURRENT version and an OLDER version.
            
            Determine if the older version contains contradictory, conflicting, or superseded rules/values compared to the current version regarding the policy or query topic.
            
            Return a JSON object with:
            - "conflictDetected": true or false
            - "conflictDescription": concise explanation of the contradictory values (or null if false)
            
            Output valid JSON only. Do not wrap in markdown backticks.
            """;

    public static final String CONFLICT_USER_TEMPLATE = """
            Document Name: {documentName}
            Policy Topic / Query: {query}
            
            Current Version ({currentVersion}):
            {currentContent}
            
            Older Version ({olderVersion}):
            {olderContent}
            
            Analyze whether the older version contains contradictory values for this policy. Output JSON only.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public ConflictDetectionServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<PolicyConflictDto> detectConflict(
            String query,
            List<SearchResultDto> currentVersionChunks,
            List<SearchResultDto> olderVersionChunks) {

        if (currentVersionChunks == null || currentVersionChunks.isEmpty()
                || olderVersionChunks == null || olderVersionChunks.isEmpty()) {
            return Optional.empty();
        }

        // Group chunks by normalized document name
        Map<String, List<SearchResultDto>> currentByDoc = groupChunksByDocName(currentVersionChunks);
        Map<String, List<SearchResultDto>> olderByDoc = groupChunksByDocName(olderVersionChunks);

        for (Map.Entry<String, List<SearchResultDto>> entry : olderByDoc.entrySet()) {
            String docKey = entry.getKey();
            List<SearchResultDto> olderChunks = entry.getValue();
            List<SearchResultDto> currentChunks = currentByDoc.get(docKey);

            if (currentChunks == null || currentChunks.isEmpty()) {
                continue;
            }

            String docName = currentChunks.get(0).getDocumentName() != null
                    ? currentChunks.get(0).getDocumentName()
                    : olderChunks.get(0).getDocumentName();

            String currentVersion = formatVersionLabel(currentChunks.get(0));
            String olderVersion = formatVersionLabel(olderChunks.get(0));

            String currentContent = currentChunks.stream()
                    .map(SearchResultDto::getChunkText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));

            String olderContent = olderChunks.stream()
                    .map(SearchResultDto::getChunkText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));

            try {
                PromptTemplate userTemplate = new PromptTemplate(CONFLICT_USER_TEMPLATE);
                String renderedUserPrompt = userTemplate.render(Map.of(
                        "documentName", docName != null ? docName : "Enterprise Document",
                        "query", query != null ? query : "",
                        "currentVersion", currentVersion,
                        "olderVersion", olderVersion,
                        "currentContent", currentContent,
                        "olderContent", olderContent
                ));

                ChatClient chatClient = chatClientBuilder.build();
                String rawLlmResponse = chatClient.prompt()
                        .system(CONFLICT_SYSTEM_TEMPLATE)
                        .user(renderedUserPrompt)
                        .call()
                        .content();

                Optional<PolicyConflictDto> parsedConflict = parseConflictResponse(
                        rawLlmResponse, docName, currentVersion, olderVersion
                );

                if (parsedConflict.isPresent()) {
                    log.info("Conflict detected for document '{}' between {} and {}: {}",
                            docName, currentVersion, olderVersion, parsedConflict.get().getConflictDescription());
                    return parsedConflict;
                }
            } catch (Exception e) {
                log.warn("Error running conflict detection with Groq LLM: {}. Falling back to heuristic check.", e.getMessage());
                // Fallback heuristic: check if texts contain contradictory policy numbers/keywords
                Optional<PolicyConflictDto> heuristicConflict = checkHeuristicConflict(
                        docName, currentVersion, olderVersion, currentContent, olderContent
                );
                if (heuristicConflict.isPresent()) {
                    return heuristicConflict;
                }
            }
        }

        return Optional.empty();
    }

    private Optional<PolicyConflictDto> parseConflictResponse(
            String rawResponse, String docName, String currentVersion, String olderVersion) {

        if (rawResponse == null || rawResponse.isBlank()) {
            return Optional.empty();
        }

        try {
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonNode root = objectMapper.readTree(cleaned);
            boolean conflictDetected = root.path("conflictDetected").asBoolean(false)
                    || root.path("hasConflict").asBoolean(false);

            if (conflictDetected) {
                String description = root.path("conflictDescription").asText(
                        "Contradictory policy values detected between versions."
                );

                String warning = String.format(
                        "Warning: Conflict detected between current version (%s) and older version (%s) of '%s'. %s. Answer was generated using the current version.",
                        currentVersion, olderVersion, docName, description
                );

                return Optional.of(PolicyConflictDto.builder()
                        .documentName(docName)
                        .currentVersion(currentVersion)
                        .olderVersion(olderVersion)
                        .conflictDescription(description)
                        .warning(warning)
                        .build());
            }
        } catch (Exception e) {
            log.debug("Could not parse conflict response as JSON: '{}'. Checking text content.", rawResponse);
            if (rawResponse.toLowerCase().contains("conflict")
                    && (rawResponse.toLowerCase().contains("contradict") || rawResponse.toLowerCase().contains("supersede"))) {
                String warning = String.format(
                        "Warning: Conflict detected between current version (%s) and older version (%s) of '%s'. Answer was generated using the current version.",
                        currentVersion, olderVersion, docName
                );
                return Optional.of(PolicyConflictDto.builder()
                        .documentName(docName)
                        .currentVersion(currentVersion)
                        .olderVersion(olderVersion)
                        .conflictDescription(rawResponse.trim())
                        .warning(warning)
                        .build());
            }
        }

        return Optional.empty();
    }

    private Optional<PolicyConflictDto> checkHeuristicConflict(
            String docName, String currentVersion, String olderVersion, String currentContent, String olderContent) {

        if (currentContent.equalsIgnoreCase(olderContent)) {
            return Optional.empty();
        }

        // Detect obvious contradictory policy values like days/numbers/amounts
        if (!currentContent.equals(olderContent)
                && (currentContent.toLowerCase().contains("day") || currentContent.toLowerCase().contains("policy")
                || currentContent.toLowerCase().contains("password") || currentContent.toLowerCase().contains("allowance")
                || currentContent.toLowerCase().contains("require"))) {

            String description = String.format(
                    "Older version (%s) specifies differing policy values compared to current version (%s).",
                    olderVersion, currentVersion
            );
            String warning = String.format(
                    "Warning: Conflict detected between current version (%s) and older version (%s) of '%s'. %s. Answer was generated using the current version.",
                    currentVersion, olderVersion, docName, description
            );

            return Optional.of(PolicyConflictDto.builder()
                    .documentName(docName)
                    .currentVersion(currentVersion)
                    .olderVersion(olderVersion)
                    .conflictDescription(description)
                    .warning(warning)
                    .build());
        }

        return Optional.empty();
    }

    private Map<String, List<SearchResultDto>> groupChunksByDocName(List<SearchResultDto> chunks) {
        Map<String, List<SearchResultDto>> map = new LinkedHashMap<>();
        for (SearchResultDto chunk : chunks) {
            String name = chunk.getDocumentName() != null ? chunk.getDocumentName().trim().toLowerCase() : "";
            map.computeIfAbsent(name, k -> new ArrayList<>()).add(chunk);
        }
        return map;
    }

    private String formatVersionLabel(SearchResultDto chunk) {
        if (chunk.getVersionNumber() != null) {
            return "v" + chunk.getVersionNumber();
        } else if (chunk.getEffectiveDate() != null) {
            return chunk.getEffectiveDate().toString();
        }
        return "Unknown Version";
    }
}
