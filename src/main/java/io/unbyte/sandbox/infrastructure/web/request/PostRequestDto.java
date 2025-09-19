package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for post requests
 * Maps to domain post IDs
 */
public record PostRequestDto(
    @JsonProperty("postId")
    @NotNull(message = "Post ID cannot be null")
    String postId
) {
    
    /**
     * Compact constructor for validation
     */
    public PostRequestDto {
        if (postId == null || postId.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
    }
}
