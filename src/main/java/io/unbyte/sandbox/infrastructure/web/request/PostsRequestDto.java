package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for multiple posts
 * Contains a list of post requests
 */
public class PostsRequestDto {

    @JsonProperty("request")
    @NotNull(message = "Request cannot be null")
    @NotEmpty(message = "Request cannot be empty")
    @Valid
    private List<PostRequestDto> request;

    public PostsRequestDto() {}

    public PostsRequestDto(List<PostRequestDto> request) {
        this.request = request;
    }

    public List<PostRequestDto> getRequest() {
        return request;
    }

    public void setRequest(List<PostRequestDto> request) {
        this.request = request;
    }
}
