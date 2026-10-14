package com.enterprise.rag.dto;

import java.util.List;

public class DocumentSummaryDto {
    private String id;
    private String name;
    private String documentName;
    private String department;
    private Integer currentVersion;
    private String lastUpdated;
    private Integer totalVersions;
    private List<DocumentVersionDto> versions;

    public DocumentSummaryDto() {
    }

    public DocumentSummaryDto(String id, String name, String documentName, String department,
                              Integer currentVersion, String lastUpdated, Integer totalVersions,
                              List<DocumentVersionDto> versions) {
        this.id = id;
        this.name = name;
        this.documentName = documentName;
        this.department = department;
        this.currentVersion = currentVersion;
        this.lastUpdated = lastUpdated;
        this.totalVersions = totalVersions;
        this.versions = versions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    public Integer getTotalVersions() { return totalVersions; }
    public void setTotalVersions(Integer totalVersions) { this.totalVersions = totalVersions; }
    public List<DocumentVersionDto> getVersions() { return versions; }
    public void setVersions(List<DocumentVersionDto> versions) { this.versions = versions; }
}
