package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO for individual posts
 * Contains post ID and its comments
 */
@Schema(description = "Post with comments")
public record PostResponseDto(
        @JsonProperty("id") @Schema(description = "Post identifier", example = "1") String id,
        @JsonProperty("comments") @Schema(description = "Comments on the post")
                List<CommentResponseDto> comments) {

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
