package com.enterprise.rag.dto;

import java.time.OffsetDateTime;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, OffsetDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : OffsetDateTime.now();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;
        private OffsetDateTime timestamp = OffsetDateTime.now();

        public Builder<T> success(boolean success) { this.success = success; return this; }
        public Builder<T> message(String message) { this.message = message; return this; }
        public Builder<T> data(T data) { this.data = data; return this; }
        public Builder<T> timestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ApiResponse<T> build() {
            return new ApiResponse<>(success, message, data, timestamp);
        }
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operation successful", data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, OffsetDateTime.now());
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
}
