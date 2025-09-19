package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for individual posts
 * Contains post ID and its comments
 */
public record PostResponseDto(
    @JsonProperty("id") String id,
    @JsonProperty("comments") List<CommentResponseDto> comments
) {
    
    /**
     * Compact constructor for validation
     */
    public PostResponseDto {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
        if (comments == null) {
            comments = List.of(); // Empty list instead of null
        }
    }
}
