package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.PdfChunkDto;
import com.enterprise.rag.dto.PdfUploadResponse;
import com.enterprise.rag.exception.RagProcessingException;
import com.enterprise.rag.service.PdfDocumentService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PdfDocumentServiceImpl.class);

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 100;

    @Override
    public PdfUploadResponse processPdfAndChunk(MultipartFile file) {
        validatePdfFile(file);

        String extractedText = extractTextFromPdf(file);
        List<PdfChunkDto> chunks = splitIntoChunks(extractedText, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);

        log.info("Processed PDF '{}': extracted {} characters into {} chunks",
                file.getOriginalFilename(), extractedText.length(), chunks.size());

        return PdfUploadResponse.builder()
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .totalCharacters(extractedText.length())
                .totalChunks(chunks.size())
                .chunks(chunks)
                .build();
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported. Received: " + originalFilename);
        }
    }

    private String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new RagProcessingException("Encrypted PDF files are not supported: " + file.getOriginalFilename());
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);

            if (rawText == null || rawText.isBlank()) {
                return "";
            }

            // Normalize line endings and multiple spaces
            return rawText.replace("\r\n", "\n")
                    .replaceAll("[ \\t]+", " ")
                    .replaceAll("\\n{3,}", "\n\n")
                    .trim();

        } catch (IOException e) {
            log.error("Failed to parse PDF document: {}", file.getOriginalFilename(), e);
            throw new RagProcessingException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Splits normalized text into overlapping chunks of approximately {@code chunkSize}
     * characters with {@code overlap} characters shared between consecutive chunks.
     * Searches for natural sentence or whitespace boundaries near the target size.
     */
    private List<PdfChunkDto> splitIntoChunks(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<PdfChunkDto> chunks = new ArrayList<>();
        int length = text.length();

        // If total text fits into a single chunk
        if (length <= chunkSize) {
            chunks.add(PdfChunkDto.builder()
                    .chunkIndex(0)
                    .text(text)
                    .characterCount(length)
                    .startOffset(0)
                    .endOffset(length)
                    .build());
            return chunks;
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < length) {
            int idealEnd = Math.min(start + chunkSize, length);
            int actualEnd = idealEnd;

            // If not at the end of text, attempt to break on a sentence or word boundary
            if (idealEnd < length) {
                int boundary = findNaturalBreakBoundary(text, start, idealEnd);
                if (boundary > start) {
                    actualEnd = boundary;
                }
            }

            String chunkContent = text.substring(start, actualEnd).trim();
            if (!chunkContent.isEmpty()) {
                chunks.add(PdfChunkDto.builder()
                        .chunkIndex(chunkIndex++)
                        .text(chunkContent)
                        .characterCount(chunkContent.length())
                        .startOffset(start)
                        .endOffset(actualEnd)
                        .build());
            }

            if (actualEnd >= length) {
                break;
            }

            // Advance start offset with overlap
            int nextStart = actualEnd - overlap;
            // Prevent stagnation
            if (nextStart <= start) {
                nextStart = start + Math.max(1, chunkSize - overlap);
            }
            start = nextStart;
        }

        return chunks;
    }

    private int findNaturalBreakBoundary(String text, int start, int idealEnd) {
        // Search backwards from idealEnd (up to 70 chars) for sentence terminators or newlines
        int searchWindowStart = Math.max(start + (DEFAULT_CHUNK_SIZE - 70), idealEnd - 70);

        for (int i = idealEnd; i >= searchWindowStart; i--) {
            char c = text.charAt(i - 1);
            if (c == '\n' || c == '.' || c == '?' || c == '!') {
                return i;
            }
        }

        // Fallback: search backwards for space
        for (int i = idealEnd; i >= searchWindowStart; i--) {
            if (Character.isWhitespace(text.charAt(i - 1))) {
                return i;
            }
        }

        // If no natural break found, hard split at idealEnd
        return idealEnd;
    }
}
