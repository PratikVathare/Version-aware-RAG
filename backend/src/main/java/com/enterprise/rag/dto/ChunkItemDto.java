package com.enterprise.rag.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ChunkItemDto {

    private Integer chunkIndex;

    @JsonAlias({"chunkText", "content"})
    private String text;

    private String documentName;
    private Integer versionNumber;

    public ChunkItemDto() {
    }

    public ChunkItemDto(Integer chunkIndex, String text, String documentName, Integer versionNumber) {
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer chunkIndex;
        private String text;
        private String documentName;
        private Integer versionNumber;

        public Builder chunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }

        public ChunkItemDto build() {
            return new ChunkItemDto(chunkIndex, text, documentName, versionNumber);
        }
    }

    public String resolveText() {
        return text != null ? text.trim() : "";
    }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
}
