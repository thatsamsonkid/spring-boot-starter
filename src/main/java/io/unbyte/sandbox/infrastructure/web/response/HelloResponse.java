package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for /hello endpoint using Java Record
 *
 * Records work perfectly with Jackson annotations
 */
public record HelloResponse(
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("service") String service) {

    /**
     * Compact constructor for validation
     */
    public HelloResponse {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
        if (service == null || service.trim().isEmpty()) {
            throw new IllegalArgumentException("Service cannot be null or empty");
        }
    }

    /**
     * Factory method for common use cases
     */
    public static HelloResponse success(String message, String timestamp) {
        return new HelloResponse(message, timestamp, "sandbox");
    }
}
