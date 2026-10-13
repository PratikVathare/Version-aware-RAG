package com.enterprise.rag.dto;

import java.util.List;

public class RagQueryResponse {

    private String answer;
    private String query;
    private String resolvedVersion;
    private List<DocumentChunkDto> sourceChunks;
    private Long latencyMs;

    public RagQueryResponse() {
    }

    public RagQueryResponse(String answer, String query, String resolvedVersion, List<DocumentChunkDto> sourceChunks, Long latencyMs) {
        this.answer = answer;
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
        private String query;
        private String resolvedVersion;
        private List<DocumentChunkDto> sourceChunks;
        private Long latencyMs;

        public Builder answer(String answer) { this.answer = answer; return this; }
        public Builder query(String query) { this.query = query; return this; }
        public Builder resolvedVersion(String resolvedVersion) { this.resolvedVersion = resolvedVersion; return this; }
        public Builder sourceChunks(List<DocumentChunkDto> sourceChunks) { this.sourceChunks = sourceChunks; return this; }
        public Builder latencyMs(Long latencyMs) { this.latencyMs = latencyMs; return this; }

        public RagQueryResponse build() {
            return new RagQueryResponse(answer, query, resolvedVersion, sourceChunks, latencyMs);
        }
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getResolvedVersion() { return resolvedVersion; }
    public void setResolvedVersion(String resolvedVersion) { this.resolvedVersion = resolvedVersion; }

    public List<DocumentChunkDto> getSourceChunks() { return sourceChunks; }
    public void setSourceChunks(List<DocumentChunkDto> sourceChunks) { this.sourceChunks = sourceChunks; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
}
