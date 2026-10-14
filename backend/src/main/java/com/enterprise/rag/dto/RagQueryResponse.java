package com.enterprise.rag.dto;

import java.time.LocalDate;
import java.util.List;

public class RagQueryResponse {

    private String answer;
    private String documentName;
    private Integer versionNumber;
    private LocalDate effectiveDate;
    private String currentVersion;
    private String olderVersion;
    private String conflictDescription;
    private String warning;
    private PolicyConflictDto conflict;
    private String query;
    private String resolvedVersion;
    private List<DocumentChunkDto> sourceChunks;
    private Long latencyMs;

    public RagQueryResponse() {
    }

    public RagQueryResponse(String answer, String documentName, Integer versionNumber, LocalDate effectiveDate,
                            String currentVersion, String olderVersion, String conflictDescription, String warning,
                            PolicyConflictDto conflict, String query, String resolvedVersion,
                            List<DocumentChunkDto> sourceChunks, Long latencyMs) {
        this.answer = answer;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.effectiveDate = effectiveDate;
        this.currentVersion = currentVersion;
        this.olderVersion = olderVersion;
        this.conflictDescription = conflictDescription;
        this.warning = warning;
        this.conflict = conflict;
        this.query = query;
        this.resolvedVersion = resolvedVersion;
        this.sourceChunks = sourceChunks;
        this.latencyMs = latencyMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String answer;
        private String documentName;
        private Integer versionNumber;
        private LocalDate effectiveDate;
        private String currentVersion;
        private String olderVersion;
        private String conflictDescription;
        private String warning;
        private PolicyConflictDto conflict;
        private String query;
        private String resolvedVersion;
        private List<DocumentChunkDto> sourceChunks;
        private Long latencyMs;

        public Builder answer(String answer) { this.answer = answer; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder effectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public Builder currentVersion(String currentVersion) { this.currentVersion = currentVersion; return this; }
        public Builder olderVersion(String olderVersion) { this.olderVersion = olderVersion; return this; }
        public Builder conflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; return this; }
        public Builder warning(String warning) { this.warning = warning; return this; }
        public Builder conflict(PolicyConflictDto conflict) { this.conflict = conflict; return this; }
        public Builder query(String query) { this.query = query; return this; }
        public Builder resolvedVersion(String resolvedVersion) { this.resolvedVersion = resolvedVersion; return this; }
        public Builder sourceChunks(List<DocumentChunkDto> sourceChunks) { this.sourceChunks = sourceChunks; return this; }
        public Builder latencyMs(Long latencyMs) { this.latencyMs = latencyMs; return this; }

        public RagQueryResponse build() {
            return new RagQueryResponse(
                    answer, documentName, versionNumber, effectiveDate,
                    currentVersion, olderVersion, conflictDescription, warning,
                    conflict, query, resolvedVersion, sourceChunks, latencyMs
            );
        }
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }

    public String getOlderVersion() { return olderVersion; }
    public void setOlderVersion(String olderVersion) { this.olderVersion = olderVersion; }

    public String getConflictDescription() { return conflictDescription; }
    public void setConflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    public PolicyConflictDto getConflict() { return conflict; }
    public void setConflict(PolicyConflictDto conflict) { this.conflict = conflict; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getResolvedVersion() { return resolvedVersion; }
    public void setResolvedVersion(String resolvedVersion) { this.resolvedVersion = resolvedVersion; }

    public List<DocumentChunkDto> getSourceChunks() { return sourceChunks; }
    public void setSourceChunks(List<DocumentChunkDto> sourceChunks) { this.sourceChunks = sourceChunks; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
}
