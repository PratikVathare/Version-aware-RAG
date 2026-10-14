package com.enterprise.rag.dto;

public class DocumentVersionDto {
    private String id;
    private Integer versionNumber;
    private String effectiveDate;
    private String department;
    private Boolean isCurrent;
    private Integer totalChunks;
    private Integer chunksCount;
    private String createdAt;

    public DocumentVersionDto() {
    }

    public DocumentVersionDto(String id, Integer versionNumber, String effectiveDate, String department,
                              Boolean isCurrent, Integer totalChunks, Integer chunksCount, String createdAt) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.effectiveDate = effectiveDate;
        this.department = department;
        this.isCurrent = isCurrent;
        this.totalChunks = totalChunks;
        this.chunksCount = chunksCount;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }
    public Integer getChunksCount() { return chunksCount; }
    public void setChunksCount(Integer chunksCount) { this.chunksCount = chunksCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
