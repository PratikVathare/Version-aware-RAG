package com.enterprise.rag.entity;

import com.enterprise.rag.config.PGvectorAttributeConverter;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "chunk_text", columnDefinition = "TEXT", nullable = false)
    private String chunkText;

    // Vector embedding for sentence-transformers/all-MiniLM-L6-v2 (384 dimensions)
    @Column(name = "embedding", columnDefinition = "vector(384)")
    @JdbcTypeCode(SqlTypes.OTHER)
    @Convert(converter = PGvectorAttributeConverter.class)
    private PGvector embedding;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public DocumentChunk() {
    }

    public DocumentChunk(UUID id, String documentName, Integer versionNumber, LocalDate effectiveDate,
                         String department, String chunkText, PGvector embedding, OffsetDateTime createdAt) {
        this.id = id;
        this.documentName = documentName;
        this.versionNumber = versionNumber;
        this.effectiveDate = effectiveDate;
        this.department = department;
        this.chunkText = chunkText;
        this.embedding = embedding;
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
        private PGvector embedding;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder effectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder chunkText(String chunkText) { this.chunkText = chunkText; return this; }
        public Builder embedding(PGvector embedding) { this.embedding = embedding; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DocumentChunk build() {
            return new DocumentChunk(id, documentName, versionNumber, effectiveDate, department, chunkText, embedding, createdAt);
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; return; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; return; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; return; }

    public PGvector getEmbedding() { return embedding; }
    public void setEmbedding(PGvector embedding) { this.embedding = embedding; return; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return; }
}
