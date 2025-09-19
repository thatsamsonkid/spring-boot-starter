package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for /health endpoint using Java Record
 */
public record HealthResponse(
    @JsonProperty("status") String status,
    @JsonProperty("timestamp") String timestamp,
    @JsonProperty("service") String service,
    @JsonProperty("version") String version
) {
    
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
