package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for post requests
 * Maps to domain post IDs
 */
@Schema(description = "Single post identifier to fetch")
public record PostRequestDto(
        @JsonProperty("postId")
                @NotNull(message = "Post ID cannot be null")
                @Schema(description = "Post identifier", example = "1")
                String postId) {

    /**
     * Compact constructor for validation
     */
    public PostRequestDto {
        if (postId == null || postId.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
    }
}
