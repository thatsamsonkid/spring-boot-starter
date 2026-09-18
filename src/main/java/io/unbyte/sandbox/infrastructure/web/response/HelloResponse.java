package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for /hello endpoint using Java Record
 *
 * Records work perfectly with Jackson annotations
 */
@Schema(description = "Hello endpoint response")
public record HelloResponse(
        @JsonProperty("message") @Schema(description = "Greeting including processed IDs")
                String message,
        @JsonProperty("timestamp") @Schema(description = "Response timestamp in ISO-8601 format")
                String timestamp,
        @JsonProperty("service") @Schema(description = "Service name", example = "sandbox-service")
                String service) {

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
