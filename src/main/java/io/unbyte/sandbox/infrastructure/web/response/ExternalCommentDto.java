package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for external API comment response
 * Maps from JSONPlaceholder API response
 */
public record ExternalCommentDto(
    @JsonProperty("postId") String postId,
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("body") String body
) {
    
    /**
     * Compact constructor for validation
     */
    public ExternalCommentDto {
        if (postId == null) {
            postId = "";
        }
        if (id == null) {
            id = "";
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
