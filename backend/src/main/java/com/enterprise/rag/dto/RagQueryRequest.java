package com.enterprise.rag.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class RagQueryRequest {

    @NotBlank(message = "Query prompt is required")
    private String query;

    private String documentName;
    private Integer versionNumber;
    private String department;
    private LocalDate asOfDate;
    private Integer topK;

    public RagQueryRequest() {
    }

    public RagQueryRequest(String query, String documentName, Integer versionNumber, String department, LocalDate asOfDate, Integer topK) {
        this.query = query;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.department = department;
        this.asOfDate = asOfDate;
        this.topK = topK;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String query;
        private String documentName;
        private Integer versionNumber;
        private String department;
        private LocalDate asOfDate;
        private Integer topK;

        public Builder query(String query) { this.query = query; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder asOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; return this; }
        public Builder topK(Integer topK) { this.topK = topK; return this; }

        public RagQueryRequest build() {
            return new RagQueryRequest(query, documentName, versionNumber, department, asOfDate, topK);
        }
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; }

    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }
}
