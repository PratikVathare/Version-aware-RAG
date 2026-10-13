package com.enterprise.rag.service;

import com.enterprise.rag.dto.DocumentChunkDto;
import com.enterprise.rag.dto.DocumentUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentService {

    List<DocumentChunkDto> ingestDocument(DocumentUploadRequest request);

    List<DocumentChunkDto> ingestPdfDocument(
            MultipartFile file,
            String documentName,
            Integer versionNumber,
            LocalDate effectiveDate,
            String department
    );

    List<DocumentChunkDto> getChunksByDocumentAndVersion(String documentName, Integer versionNumber);

    List<DocumentChunkDto> getChunksByDepartment(String department);

    void deleteDocumentVersion(String documentName, Integer versionNumber);

    DocumentChunkDto getChunkById(UUID id);
}
