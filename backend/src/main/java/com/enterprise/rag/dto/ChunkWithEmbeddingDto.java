package com.enterprise.rag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChunkWithEmbeddingDto {

    private Integer chunkIndex;
    private String text;
    private String documentName;
    private Integer versionNumber;
    private Integer dimension;
    private List<Float> embedding;

    public ChunkWithEmbeddingDto() {
    }

    public ChunkWithEmbeddingDto(Integer chunkIndex, String text, String documentName, Integer versionNumber, Integer dimension, List<Float> embedding) {
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.dimension = dimension;
        this.embedding = embedding;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer chunkIndex;
        private String text;
        private String documentName;
        private Integer versionNumber;
        private Integer dimension;
        private List<Float> embedding;

        public Builder chunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder dimension(Integer dimension) { this.dimension = dimension; return this; }
        public Builder embedding(List<Float> embedding) { this.embedding = embedding; return this; }

        public ChunkWithEmbeddingDto build() {
            return new ChunkWithEmbeddingDto(chunkIndex, text, documentName, versionNumber, dimension, embedding);
        }
    }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public Integer getDimension() { return dimension; }
    public void setDimension(Integer dimension) { this.dimension = dimension; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
