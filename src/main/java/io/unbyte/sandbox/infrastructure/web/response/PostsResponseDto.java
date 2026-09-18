package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO for multiple posts
 * Contains the final response structure
 */
@Schema(description = "Posts fetch response")
public record PostsResponseDto(
        @JsonProperty("data") @Schema(description = "Response payload") DataDto data) {

    /**
     * Compact constructor for validation
     */
    public PostsResponseDto {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
    }

    /**
     * Inner data structure
     */
    public record DataDto(@JsonProperty("posts") List<PostResponseDto> posts) {
        public DataDto {
            if (posts == null) {
                posts = List.of(); // Empty list instead of null
            }
        }
    }
}
