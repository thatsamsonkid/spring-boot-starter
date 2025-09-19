package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for individual comments
 * Maps from domain Comment to JSON response
 */
public record CommentResponseDto(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("body") String body
) {
    
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
