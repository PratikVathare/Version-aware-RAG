package com.enterprise.rag.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DocumentChunkDto {

    private UUID id;
    private String documentName;
    private Integer versionNumber;
    private LocalDate effectiveDate;
    private String department;
    private String chunkText;
    private Double score;
    private OffsetDateTime createdAt;

    public DocumentChunkDto() {
    }

    public DocumentChunkDto(UUID id, String documentName, Integer versionNumber, LocalDate effectiveDate,
                            String department, String chunkText, Double score, OffsetDateTime createdAt) {
        this.id = id;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.effectiveDate = effectiveDate;
        this.department = department;
        this.chunkText = chunkText;
        this.score = score;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String documentName;
        private Integer versionNumber;
        private LocalDate effectiveDate;
        private String department;
        private String chunkText;
        private Double score;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder effectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder chunkText(String chunkText) { this.chunkText = chunkText; return this; }
        public Builder score(Double score) { this.score = score; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DocumentChunkDto build() {
            return new DocumentChunkDto(id, documentName, versionNumber, effectiveDate, department, chunkText, score, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
