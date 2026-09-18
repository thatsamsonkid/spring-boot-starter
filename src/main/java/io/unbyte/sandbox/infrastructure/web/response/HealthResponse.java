package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for /health endpoint using Java Record
 */
@Schema(description = "Service health status")
public record HealthResponse(
        @JsonProperty("status") @Schema(description = "Health status", example = "UP")
                String status,
        @JsonProperty("timestamp") @Schema(description = "Response timestamp in ISO-8601 format")
                String timestamp,
        @JsonProperty("service") @Schema(description = "Service name", example = "sandbox-service")
                String service,
        @JsonProperty("version") @Schema(description = "Service version", example = "1.0.0")
                String version) {

    /**
     * Compact constructor for validation
     */
    public HealthResponse {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
        if (service == null || service.trim().isEmpty()) {
            throw new IllegalArgumentException("Service cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
    }

    /**
     * Factory method for healthy status
     */
    public static HealthResponse healthy(String timestamp, String version) {
        return new HealthResponse("UP", timestamp, "sandbox", version);
    }

    /**
     * Factory method for unhealthy status
     */
    public static HealthResponse unhealthy(String timestamp, String version) {
        return new HealthResponse("DOWN", timestamp, "sandbox", version);
    }
}
