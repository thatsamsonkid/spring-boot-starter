package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request item DTO using Java Record
 * This replaces the inner class RequestItem from HelloRequest
 */
@Schema(description = "Item included in a hello request")
public record RequestItemRecord(
        @JsonProperty("id")
                @NotNull(message = "ID cannot be null")
                @Schema(description = "Item identifier", example = "3123")
                String id) {

    /**
     * Compact constructor for validation
     */
    public RequestItemRecord {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
    }
}
