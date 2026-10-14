package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.DocumentChunkDto;
import com.enterprise.rag.dto.DocumentUploadRequest;
import com.enterprise.rag.dto.PdfChunkDto;
import com.enterprise.rag.dto.PdfUploadResponse;
import com.enterprise.rag.entity.DocumentChunk;
import com.enterprise.rag.exception.ResourceNotFoundException;
import com.enterprise.rag.repository.DocumentChunkRepository;
import com.enterprise.rag.service.DocumentService;
import com.enterprise.rag.service.EmbeddingService;
import com.enterprise.rag.service.PdfDocumentService;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 100;

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final PdfDocumentService pdfDocumentService;

    public DocumentServiceImpl(DocumentChunkRepository documentChunkRepository,
                               EmbeddingService embeddingService,
                               PdfDocumentService pdfDocumentService) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
        this.pdfDocumentService = pdfDocumentService;
    }

    @Override
    @Transactional
    public List<DocumentChunkDto> ingestDocument(DocumentUploadRequest request) {
        log.info("Ingesting document '{}' (v{}) for department '{}'",
                request.getDocumentName(), request.getVersionNumber(), request.getDepartment());

        List<String> chunkTexts = splitRawText(request.getContent(), DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
        if (chunkTexts.isEmpty()) {
            throw new IllegalArgumentException("Document content resulted in 0 chunks");
        }

        // Generate 384-dimensional pgvector embeddings via embedding service
        List<PGvector> embeddings = embeddingService.generateBatchEmbeddings(chunkTexts);

        // Build entities to store in Supabase PostgreSQL
        List<DocumentChunk> entities = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < chunkTexts.size(); i++) {
            entities.add(DocumentChunk.builder()
                    .documentName(request.getDocumentName())
                    .versionNumber(request.getVersionNumber())
                    .effectiveDate(request.getEffectiveDate())
                    .department(request.getDepartment())
                    .chunkText(chunkTexts.get(i))
                    .embedding(embeddings.get(i))
                    .createdAt(now)
                    .build());
        }

        // Persist every chunk using Spring Data JPA
        List<DocumentChunk> saved = documentChunkRepository.saveAll(entities);
        log.info("Successfully persisted {} chunks for document '{}' (v{}) in Supabase",
                saved.size(), request.getDocumentName(), request.getVersionNumber());

        return saved.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public List<DocumentChunkDto> ingestPdfDocument(
            MultipartFile file,
            String documentName,
            Integer versionNumber,
            LocalDate effectiveDate,
            String department) {

        log.info("Ingesting PDF '{}' as document '{}' (v{}) for department '{}'",
                file.getOriginalFilename(), documentName, versionNumber, department);

        // Extract and chunk PDF using Apache PDFBox
        PdfUploadResponse pdfResponse = pdfDocumentService.processPdfAndChunk(file);
        List<String> chunkTexts = pdfResponse.getChunks().stream()
                .map(PdfChunkDto::getText)
                .toList();

        if (chunkTexts.isEmpty()) {
            throw new IllegalArgumentException("PDF text extraction produced 0 chunks");
        }

        // Generate 384-dimensional pgvector embeddings
        List<PGvector> embeddings = embeddingService.generateBatchEmbeddings(chunkTexts);

        // Build entities to store in Supabase
        List<DocumentChunk> entities = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < chunkTexts.size(); i++) {
            entities.add(DocumentChunk.builder()
                    .documentName(documentName)
                    .versionNumber(versionNumber)
                    .effectiveDate(effectiveDate)
                    .department(department)
                    .chunkText(chunkTexts.get(i))
                    .embedding(embeddings.get(i))
                    .createdAt(now)
                    .build());
        }

        // Persist in Supabase
        List<DocumentChunk> saved = documentChunkRepository.saveAll(entities);
        log.info("Successfully stored {} chunks for PDF '{}' in Supabase", saved.size(), documentName);

        return saved.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunkDto> getChunksByDocumentAndVersion(String documentName, Integer versionNumber) {
        return documentChunkRepository.findByDocumentNameAndVersionNumber(documentName, versionNumber)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunkDto> getChunksByDepartment(String department) {
        return documentChunkRepository.findByDepartment(department)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteDocumentVersion(String documentName, Integer versionNumber) {
        documentChunkRepository.deleteByDocumentNameAndVersionNumber(documentName, versionNumber);
        log.info("Deleted document chunks for '{}' version {}", documentName, versionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentChunkDto getChunkById(UUID id) {
        DocumentChunk chunk = documentChunkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentChunk", "id", id));
        return mapToDto(chunk);
    }

    private DocumentChunkDto mapToDto(DocumentChunk entity) {
        return DocumentChunkDto.builder()
                .id(entity.getId())
                .documentName(entity.getDocumentName())
                .versionNumber(entity.getVersionNumber())
                .effectiveDate(entity.getEffectiveDate())
                .department(entity.getDepartment())
                .chunkText(entity.getChunkText())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private List<String> splitRawText(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = text.replace("\r\n", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        int length = normalized.length();
        if (length <= chunkSize) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < length) {
            int idealEnd = Math.min(start + chunkSize, length);
            int actualEnd = idealEnd;

            if (idealEnd < length) {
                // Find natural break
                int searchStart = Math.max(start + (chunkSize - 70), idealEnd - 70);
                for (int i = idealEnd; i >= searchStart; i--) {
                    char c = normalized.charAt(i - 1);
                    if (c == '\n' || c == '.' || c == '?' || c == '!' || Character.isWhitespace(c)) {
                        actualEnd = i;
                        break;
                    }
                }
            }

            String chunk = normalized.substring(start, actualEnd).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (actualEnd >= length) {
                break;
            }

            int nextStart = actualEnd - overlap;
            if (nextStart <= start) {
                nextStart = start + Math.max(1, chunkSize - overlap);
            }
            start = nextStart;
        }

        return chunks;
    }

    @Override
    public List<com.enterprise.rag.dto.DocumentSummaryDto> getAllDocuments() {
        List<DocumentChunk> allChunks = documentChunkRepository.findAll();
        if (allChunks.isEmpty()) {
            return Collections.emptyList();
        }

        java.util.Map<String, List<DocumentChunk>> byDocName = allChunks.stream()
                .collect(java.util.stream.Collectors.groupingBy(DocumentChunk::getDocumentName));

        List<com.enterprise.rag.dto.DocumentSummaryDto> summaries = new ArrayList<>();

        for (java.util.Map.Entry<String, List<DocumentChunk>> entry : byDocName.entrySet()) {
            String docName = entry.getKey();
            List<DocumentChunk> docChunks = entry.getValue();

            java.util.Map<Integer, List<DocumentChunk>> byVersion = docChunks.stream()
                    .collect(java.util.stream.Collectors.groupingBy(DocumentChunk::getVersionNumber));

            int maxVersion = byVersion.keySet().stream().max(Integer::compareTo).orElse(1);

            List<com.enterprise.rag.dto.DocumentVersionDto> versionDtos = new ArrayList<>();
            for (java.util.Map.Entry<Integer, List<DocumentChunk>> vEntry : byVersion.entrySet()) {
                int verNum = vEntry.getKey();
                List<DocumentChunk> vChunks = vEntry.getValue();
                DocumentChunk sample = vChunks.get(0);

                versionDtos.add(new com.enterprise.rag.dto.DocumentVersionDto(
                        docName + "-v" + verNum,
                        verNum,
                        sample.getEffectiveDate() != null ? sample.getEffectiveDate().toString() : "",
                        sample.getDepartment() != null ? sample.getDepartment() : "General",
                        verNum == maxVersion,
                        vChunks.size(),
                        vChunks.size(),
                        sample.getCreatedAt() != null ? sample.getCreatedAt().toString() : ""
                ));
            }

            versionDtos.sort((a, b) -> b.getVersionNumber().compareTo(a.getVersionNumber()));

            DocumentChunk sampleLatest = byVersion.get(maxVersion).get(0);

            summaries.add(new com.enterprise.rag.dto.DocumentSummaryDto(
                    docName,
                    docName,
                    docName,
                    sampleLatest.getDepartment() != null ? sampleLatest.getDepartment() : "General",
                    maxVersion,
                    sampleLatest.getCreatedAt() != null ? sampleLatest.getCreatedAt().toString() : "",
                    versionDtos.size(),
                    versionDtos
            ));
        }

        summaries.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return summaries;
    }
}

