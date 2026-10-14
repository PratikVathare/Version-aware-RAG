package com.enterprise.rag.dto;

public class PolicyConflictDto {

    private String documentName;
    private String currentVersion;
    private String olderVersion;
    private String conflictDescription;
    private String warning;

    public PolicyConflictDto() {
    }

    public PolicyConflictDto(String documentName, String currentVersion, String olderVersion,
                             String conflictDescription, String warning) {
        this.documentName = documentName;
        this.currentVersion = currentVersion;
        this.olderVersion = olderVersion;
        this.conflictDescription = conflictDescription;
        this.warning = warning;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String documentName;
        private String currentVersion;
        private String olderVersion;
        private String conflictDescription;
        private String warning;

        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder currentVersion(String currentVersion) { this.currentVersion = currentVersion; return this; }
        public Builder olderVersion(String olderVersion) { this.olderVersion = olderVersion; return this; }
        public Builder conflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; return this; }
        public Builder warning(String warning) { this.warning = warning; return this; }

        public PolicyConflictDto build() {
            return new PolicyConflictDto(documentName, currentVersion, olderVersion, conflictDescription, warning);
        }
    }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }

    public String getOlderVersion() { return olderVersion; }
    public void setOlderVersion(String olderVersion) { this.olderVersion = olderVersion; }

    public String getConflictDescription() { return conflictDescription; }
    public void setConflictDescription(String conflictDescription) { this.conflictDescription = conflictDescription; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
