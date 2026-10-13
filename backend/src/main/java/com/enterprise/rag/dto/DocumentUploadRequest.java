package com.enterprise.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DocumentUploadRequest {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotNull(message = "Version number is required")
    private Integer versionNumber;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Content is required")
    private String content;

    public DocumentUploadRequest() {
    }

    public DocumentUploadRequest(String documentName, Integer versionNumber, LocalDate effectiveDate, String department, String content) {
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.effectiveDate = effectiveDate;
        this.department = department;
        this.content = content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String documentName;
        private Integer versionNumber;
        private LocalDate effectiveDate;
        private String department;
        private String content;

        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder effectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder content(String content) { this.content = content; return this; }

        public DocumentUploadRequest build() {
            return new DocumentUploadRequest(documentName, versionNumber, effectiveDate, department, content);
        }
    }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
