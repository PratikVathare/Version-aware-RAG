package com.enterprise.rag.dto;

import java.util.List;

public class PythonEmbedResponse {

    private String model;
    private int dimension;
    private List<List<Float>> embeddings;

    public PythonEmbedResponse() {
    }

    public PythonEmbedResponse(String model, int dimension, List<List<Float>> embeddings) {
        this.model = model;
        this.dimension = dimension;
        this.embeddings = embeddings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model;
        private int dimension;
        private List<List<Float>> embeddings;

        public Builder model(String model) { this.model = model; return this; }
        public Builder dimension(int dimension) { this.dimension = dimension; return this; }
        public Builder embeddings(List<List<Float>> embeddings) { this.embeddings = embeddings; return this; }

        public PythonEmbedResponse build() {
            return new PythonEmbedResponse(model, dimension, embeddings);
        }
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }

    public List<List<Float>> getEmbeddings() { return embeddings; }
    public void setEmbeddings(List<List<Float>> embeddings) { this.embeddings = embeddings; }
}
