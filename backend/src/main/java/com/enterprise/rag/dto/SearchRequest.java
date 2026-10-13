package com.enterprise.rag.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class SearchRequest {

    @NotBlank(message = "Question is required")
    @JsonAlias({"query", "prompt", "text"})
    private String question;

    private Integer topK = 10;

    public SearchRequest() {
    }

    public SearchRequest(String question, Integer topK) {
        this.question = question;
        this.topK = topK != null ? topK : 10;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String question;
        private Integer topK = 10;

        public Builder question(String question) { this.question = question; return this; }
        public Builder topK(Integer topK) { this.topK = topK; return this; }

        public SearchRequest build() {
            return new SearchRequest(question, topK);
        }
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }
}
