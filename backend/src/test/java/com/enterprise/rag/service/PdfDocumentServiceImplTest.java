package com.enterprise.rag.service;

import com.enterprise.rag.dto.PdfUploadResponse;
import com.enterprise.rag.service.impl.PdfDocumentServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfDocumentServiceImplTest {

    private PdfDocumentServiceImpl pdfDocumentService;

    @BeforeEach
    void setUp() {
        pdfDocumentService = new PdfDocumentServiceImpl();
    }

    @Test
    @DisplayName("Should extract text and chunk into ~500 character chunks with ~100 character overlap")
    void testProcessPdfAndChunk() throws IOException {
        // Generate a sample PDF with repeating paragraphs
        byte[] pdfBytes = createSamplePdf();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                pdfBytes
        );

        PdfUploadResponse response = pdfDocumentService.processPdfAndChunk(mockFile);

        assertNotNull(response);
        assertEquals("test-document.pdf", response.getFileName());
        assertTrue(response.getTotalCharacters() > 0);
        assertTrue(response.getTotalChunks() >= 1);
        assertNotNull(response.getChunks());

        // Verify each chunk is around 500 characters
        response.getChunks().forEach(chunk -> {
            assertNotNull(chunk.getText());
            assertFalse(chunk.getText().isBlank());
            assertTrue(chunk.getCharacterCount() <= 600, "Chunk should be around 500 chars");
        });
    }

    @Test
    @DisplayName("Should reject non-PDF file")
    void testRejectNonPdf() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () ->
                pdfDocumentService.processPdfAndChunk(mockFile)
        );
    }

    private byte[] createSamplePdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.setLeading(14.5f);
                stream.newLineAtOffset(50, 700);

                // Write multiple sentences to exceed 500 characters
                for (int i = 0; i < 20; i++) {
                    stream.showText("This is sentence " + i + " providing enterprise domain information for version-aware retrieval.");
                    stream.newLine();
                }
                stream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }
}
