package com.enterprise.rag.service;

import com.enterprise.rag.dto.ChunkItemDto;
import com.enterprise.rag.dto.ChunkWithEmbeddingDto;
import com.pgvector.PGvector;

import java.util.List;

public interface EmbeddingService {

    PGvector generateEmbedding(String text);

    List<PGvector> generateBatchEmbeddings(List<String> texts);

    List<List<Float>> generateFloatEmbeddings(List<String> texts);

    List<ChunkWithEmbeddingDto> embedChunks(List<ChunkItemDto> chunks);

    int getEmbeddingDimension();
}
