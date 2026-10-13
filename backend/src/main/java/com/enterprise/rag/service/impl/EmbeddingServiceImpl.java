package com.enterprise.rag.service.impl;

import com.enterprise.rag.dto.ChunkItemDto;
import com.enterprise.rag.dto.ChunkWithEmbeddingDto;
import com.enterprise.rag.dto.PythonEmbedRequest;
import com.enterprise.rag.dto.PythonEmbedResponse;
import com.enterprise.rag.exception.RagProcessingException;
import com.enterprise.rag.service.EmbeddingService;
import com.enterprise.rag.util.VectorUtils;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    private final RestClient embeddingRestClient;

    public EmbeddingServiceImpl(RestClient embeddingRestClient) {
        this.embeddingRestClient = embeddingRestClient;
    }

    @Value("${rag.embedding.dimension:384}")
    private int embeddingDimension;

    @Value("${rag.embedding.model-name:sentence-transformers/all-MiniLM-L6-v2}")
    private String modelName;

    @Override
    public PGvector generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text for embedding generation cannot be empty");
        }
        List<List<Float>> embeddings = generateFloatEmbeddings(List.of(text));
        if (embeddings.isEmpty() || embeddings.get(0).isEmpty()) {
            throw new RagProcessingException("No embedding returned from embedding service for input text");
        }
        return toPgVector(embeddings.get(0));
    }

    @Override
    public List<PGvector> generateBatchEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<Float>> floatEmbeddings = generateFloatEmbeddings(texts);
        return floatEmbeddings.stream()
                .map(this::toPgVector)
                .toList();
    }

    @Override
    public List<List<Float>> generateFloatEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.debug("Calling local Python embedding service for {} texts", texts.size());

            PythonEmbedResponse response = embeddingRestClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new PythonEmbedRequest(texts))
                    .retrieve()
                    .body(PythonEmbedResponse.class);

            if (response == null || response.getEmbeddings() == null) {
                throw new RagProcessingException("Empty response from local Python embedding service");
            }

            if (response.getEmbeddings().size() != texts.size()) {
                throw new RagProcessingException(String.format(
                        "Embedding count mismatch: expected %d, got %d",
                        texts.size(), response.getEmbeddings().size()));
            }

            return response.getEmbeddings();

        } catch (RestClientException ex) {
            log.error("Failed to connect to local Python embedding service", ex);
            throw new RagProcessingException(
                    "Local Python embedding service unavailable at configured endpoint. " +
                    "Please ensure the embedding service is running (python embedding-service/app.py). " +
                    "Error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<ChunkWithEmbeddingDto> embedChunks(List<ChunkItemDto> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> texts = chunks.stream()
                .map(ChunkItemDto::resolveText)
                .toList();

        List<List<Float>> embeddings = generateFloatEmbeddings(texts);

        List<ChunkWithEmbeddingDto> result = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            ChunkItemDto chunk = chunks.get(i);
            List<Float> embedding = embeddings.get(i);

            result.add(ChunkWithEmbeddingDto.builder()
                    .chunkIndex(chunk.getChunkIndex() != null ? chunk.getChunkIndex() : i)
                    .text(chunk.resolveText())
                    .documentName(chunk.getDocumentName())
                    .versionNumber(chunk.getVersionNumber())
                    .dimension(embedding != null ? embedding.size() : embeddingDimension)
                    .embedding(embedding)
                    .build());
        }

        return result;
    }

    @Override
    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    private PGvector toPgVector(List<Float> floats) {
        if (floats == null) {
            return null;
        }
        float[] arr = new float[floats.size()];
        for (int i = 0; i < floats.size(); i++) {
            arr[i] = floats.get(i);
        }
        return VectorUtils.floatArrayToPGvector(arr);
    }
}
