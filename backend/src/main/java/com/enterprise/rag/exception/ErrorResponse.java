package com.enterprise.rag.exception;

import java.time.OffsetDateTime;
import java.util.List;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details, OffsetDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : OffsetDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> details;
        private OffsetDateTime timestamp = OffsetDateTime.now();

        public Builder status(int status) { this.status = status; return this; }
        public Builder error(String error) { this.error = error; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder details(List<String> details) { this.details = details; return this; }
        public Builder timestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(status, error, message, path, details, timestamp);
        }
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
}
