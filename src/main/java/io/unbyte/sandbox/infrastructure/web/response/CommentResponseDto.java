package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for individual comments
 * Maps from domain Comment to JSON response
 */
@Schema(description = "Comment on a post")
public record CommentResponseDto(
        @JsonProperty("id") @Schema(description = "Comment identifier") String id,
        @JsonProperty("name") @Schema(description = "Comment author name") String name,
        @JsonProperty("email") @Schema(description = "Comment author email") String email,
        @JsonProperty("body") @Schema(description = "Comment body") String body) {

    /**
     * Compact constructor for validation
     */
    public CommentResponseDto {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment ID cannot be null or empty");
        }
        if (name == null) {
            name = "";
        }
        if (email == null) {
            email = "";
        }
        if (body == null) {
            body = "";
        }
    }
}
