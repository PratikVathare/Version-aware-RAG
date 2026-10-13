package com.enterprise.rag.dto;

import java.util.List;

public class PythonEmbedRequest {

    private List<String> texts;

    public PythonEmbedRequest() {
    }

    public PythonEmbedRequest(List<String> texts) {
        this.texts = texts;
    }

    public List<String> getTexts() { return texts; }
    public void setTexts(List<String> texts) { this.texts = texts; }
}
