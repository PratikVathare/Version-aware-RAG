package com.enterprise.rag.dto;

public class PdfChunkDto {

    private int chunkIndex;
    private String text;
    private int characterCount;
    private int startOffset;
    private int endOffset;

    public PdfChunkDto() {
    }

    public PdfChunkDto(int chunkIndex, String text, int characterCount, int startOffset, int endOffset) {
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.characterCount = characterCount;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int chunkIndex;
        private String text;
        private int characterCount;
        private int startOffset;
        private int endOffset;

        public Builder chunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder characterCount(int characterCount) { this.characterCount = characterCount; return this; }
        public Builder startOffset(int startOffset) { this.startOffset = startOffset; return this; }
        public Builder endOffset(int endOffset) { this.endOffset = endOffset; return this; }

        public PdfChunkDto build() {
            return new PdfChunkDto(chunkIndex, text, characterCount, startOffset, endOffset);
        }
    }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getCharacterCount() { return characterCount; }
    public void setCharacterCount(int characterCount) { this.characterCount = characterCount; }

    public int getStartOffset() { return startOffset; }
    public void setStartOffset(int startOffset) { this.startOffset = startOffset; }

    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }
}
