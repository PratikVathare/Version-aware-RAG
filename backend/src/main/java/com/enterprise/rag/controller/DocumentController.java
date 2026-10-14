package com.enterprise.rag.controller;

import com.enterprise.rag.dto.ApiResponse;
import com.enterprise.rag.dto.DocumentChunkDto;
import com.enterprise.rag.dto.DocumentUploadRequest;
import com.enterprise.rag.dto.PdfUploadResponse;
import com.enterprise.rag.service.DocumentService;
import com.enterprise.rag.service.PdfDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/documents", "/api/v1/documents"})
@Tag(name = "Document Management", description = "REST endpoints for enterprise version-aware document ingestion and retrieval")
public class DocumentController {

    private final DocumentService documentService;
    private final PdfDocumentService pdfDocumentService;

    public DocumentController(DocumentService documentService, PdfDocumentService pdfDocumentService) {
        this.documentService = documentService;
        this.pdfDocumentService = pdfDocumentService;
    }

    @GetMapping
    @Operation(summary = "Get all distinct documents with their version summaries")
    public ResponseEntity<ApiResponse<List<com.enterprise.rag.dto.DocumentSummaryDto>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.ok(documentService.getAllDocuments()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Upload PDF and extract chunks",
            description = "Accepts a PDF file, extracts text with Apache PDFBox, and splits into ~500 char chunks with 100 char overlap."
    )
    public ResponseEntity<PdfUploadResponse> uploadAndChunkPdf(
            @RequestParam("file") MultipartFile file) {
        PdfUploadResponse response = pdfDocumentService.processPdfAndChunk(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest a new versioned document", description = "Chunks text, generates pgvector embeddings with version metadata, and stores every chunk in Supabase")
    public ResponseEntity<ApiResponse<List<DocumentChunkDto>>> ingestDocument(
            @Valid @RequestBody DocumentUploadRequest request) {
        List<DocumentChunkDto> response = documentService.ingestDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Document ingested successfully", response));
    }

    @PostMapping(value = "/ingest-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ingest a versioned PDF document into Supabase", description = "Extracts text from PDF, splits into chunks, computes 384-d embeddings, and saves all chunks with metadata to Supabase.")
    public ResponseEntity<ApiResponse<List<DocumentChunkDto>>> ingestPdfDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam("versionNumber") Integer versionNumber,
            @RequestParam("effectiveDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate effectiveDate,
            @RequestParam("department") String department) {
        List<DocumentChunkDto> response = documentService.ingestPdfDocument(file, documentName, versionNumber, effectiveDate, department);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("PDF ingested and stored in Supabase successfully", response));
    }

    @GetMapping("/version")
    @Operation(summary = "Get document chunks by document name and version")
    public ResponseEntity<ApiResponse<List<DocumentChunkDto>>> getChunksByDocumentAndVersion(
            @RequestParam String documentName,
            @RequestParam Integer versionNumber) {
        List<DocumentChunkDto> chunks = documentService.getChunksByDocumentAndVersion(documentName, versionNumber);
        return ResponseEntity.ok(ApiResponse.ok(chunks));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get document chunks by department")
    public ResponseEntity<ApiResponse<List<DocumentChunkDto>>> getChunksByDepartment(
            @PathVariable String department) {
        List<DocumentChunkDto> chunks = documentService.getChunksByDepartment(department);
        return ResponseEntity.ok(ApiResponse.ok(chunks));
    }

    @GetMapping("/chunks/{id}")
    @Operation(summary = "Get a specific chunk by UUID")
    public ResponseEntity<ApiResponse<DocumentChunkDto>> getChunkById(@PathVariable UUID id) {
        DocumentChunkDto chunk = documentService.getChunkById(id);
        return ResponseEntity.ok(ApiResponse.ok(chunk));
    }

    @DeleteMapping("/version")
    @Operation(summary = "Delete all chunks for a specific document version")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentVersion(
            @RequestParam String documentName,
            @RequestParam Integer versionNumber) {
        documentService.deleteDocumentVersion(documentName, versionNumber);
        return ResponseEntity.ok(ApiResponse.ok("Version deleted successfully", null));
    }
}
