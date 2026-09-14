package io.unbyte.sandbox.shared.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base exception for all application errors
 * Provides structured error information with context
 */
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String correlationId;
    private final String userId;
    private final LocalDateTime timestamp;
    private final Map<String, Object> context;
    private final String layer;
    private final String operation;

    public ApplicationException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null, null, null, null, null);
    }

    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, null, null, null, null, null);
    }

    public ApplicationException(
            ErrorCode errorCode,
            String message,
            Throwable cause,
            String correlationId,
            String userId,
            String layer,
            String operation,
            Map<String, Object> context) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.context = context;
        this.layer = layer;
        this.operation = operation;
    }

    // Getters
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public String getLayer() {
        return layer;
    }

    public String getOperation() {
        return operation;
    }

    /**
     * Create a builder for structured exception creation
     */
    public static Builder builder(ErrorCode errorCode, String message) {
        return new Builder(errorCode, message);
    }

    public static class Builder {
        private final ErrorCode errorCode;
        private final String message;
        private Throwable cause;
        private String correlationId;
        private String userId;
        private String layer;
        private String operation;
        private Map<String, Object> context;

        public Builder(ErrorCode errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder layer(String layer) {
            this.layer = layer;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public ApplicationException build() {
            return new ApplicationException(
                    errorCode, message, cause, correlationId, userId, layer, operation, context);
        }
    }
}
