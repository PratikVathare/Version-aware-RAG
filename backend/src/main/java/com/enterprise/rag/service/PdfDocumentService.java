package com.enterprise.rag.service;

import com.enterprise.rag.dto.PdfUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PdfDocumentService {

    /**
     * Extracts text from an uploaded PDF and splits it into overlapping chunks.
     *
     * @param file the multipart PDF file
     * @return response containing file metadata and chunks
     */
    PdfUploadResponse processPdfAndChunk(MultipartFile file);
}
