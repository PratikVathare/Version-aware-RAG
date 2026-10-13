package com.enterprise.rag.repository;

import com.enterprise.rag.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByDocumentName(String documentName);

    List<DocumentChunk> findByDocumentNameAndVersionNumber(String documentName, Integer versionNumber);

    List<DocumentChunk> findByDepartment(String department);

    List<DocumentChunk> findByEffectiveDateLessThanEqual(LocalDate effectiveDate);

    void deleteByDocumentNameAndVersionNumber(String documentName, Integer versionNumber);

    /**
     * Vector similarity search placeholder signature using pgvector cosine distance (<=>).
     * Implementation details to be wired during business logic phase.
     */
    @Query(value = """
        SELECT 
            dc.id AS id,
            dc.document_name AS documentName,
            dc.version_number AS versionNumber,
            dc.effective_date AS effectiveDate,
            dc.department AS department,
            dc.chunk_text AS chunkText,
            (1 - (dc.embedding <=> CAST(:queryEmbedding AS vector))) AS similarity,
            dc.created_at AS createdAt
        FROM document_chunks dc
        ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector) ASC
        LIMIT :topK
        """, nativeQuery = true)
    List<ChunkSimilarityProjection> searchSimilarChunks(
        @Param("queryEmbedding") String queryEmbedding,
        @Param("topK") int topK
    );

    @Query(value = """
        SELECT * FROM document_chunks dc
        ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector) ASC
        LIMIT :topK
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
        @Param("queryEmbedding") String queryEmbedding,
        @Param("topK") int topK
    );
}
