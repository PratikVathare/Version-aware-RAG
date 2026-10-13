package com.enterprise.rag.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ChunkSimilarityProjection {

    UUID getId();

    String getDocumentName();

    Integer getVersionNumber();

    LocalDate getEffectiveDate();

    String getDepartment();

    String getChunkText();

    Double getSimilarity();

    OffsetDateTime getCreatedAt();
}
