package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response for API endpoints
 */
@Schema(description = "Standardized API error payload")
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorDetails error;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("path")
    private String path;

    public ErrorResponse() {}

    public ErrorResponse(
            ErrorDetails error, LocalDateTime timestamp, String correlationId, String path) {
        this.error = error;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.path = path;
    }

    @Schema(description = "Error details")
    public static class ErrorDetails {
        @JsonProperty("code")
        @Schema(description = "Stable error code", example = "DOMAIN_001")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("layer")
        private String layer;

        @JsonProperty("operation")
        private String operation;

        @JsonProperty("context")
        private Map<String, Object> context;

        public ErrorDetails() {}

        public ErrorDetails(
                String code,
                String message,
                String layer,
                String operation,
                Map<String, Object> context) {
            this.code = code;
            this.message = message;
            this.layer = layer;
            this.operation = operation;
            this.context = context;
        }

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLayer() {
            return layer;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public void setContext(Map<String, Object> context) {
            this.context = context;
        }
    }

    // Getters and setters
    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
