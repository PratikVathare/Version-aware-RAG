package com.enterprise.rag.dto;

import java.util.List;

public class PdfUploadResponse {

    private String fileName;
    private long fileSize;
    private int totalCharacters;
    private int totalChunks;
    private List<PdfChunkDto> chunks;

    public PdfUploadResponse() {
    }

    public PdfUploadResponse(String fileName, long fileSize, int totalCharacters, int totalChunks, List<PdfChunkDto> chunks) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.totalCharacters = totalCharacters;
        this.totalChunks = totalChunks;
        this.chunks = chunks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileName;
        private long fileSize;
        private int totalCharacters;
        private int totalChunks;
        private List<PdfChunkDto> chunks;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder totalCharacters(int totalCharacters) { this.totalCharacters = totalCharacters; return this; }
        public Builder totalChunks(int totalChunks) { this.totalChunks = totalChunks; return this; }
        public Builder chunks(List<PdfChunkDto> chunks) { this.chunks = chunks; return this; }

        public PdfUploadResponse build() {
            return new PdfUploadResponse(fileName, fileSize, totalCharacters, totalChunks, chunks);
        }
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getTotalCharacters() { return totalCharacters; }
    public void setTotalCharacters(int totalCharacters) { this.totalCharacters = totalCharacters; }

    public int getTotalChunks() { return totalChunks; }
    public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }

    public List<PdfChunkDto> getChunks() { return chunks; }
    public void setChunks(List<PdfChunkDto> chunks) { this.chunks = chunks; }
}
