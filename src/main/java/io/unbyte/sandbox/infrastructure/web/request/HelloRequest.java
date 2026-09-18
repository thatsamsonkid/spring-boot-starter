package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for /hello endpoint
 * Updated to use RequestItemRecord instead of inner class
 */
@Schema(description = "Hello request containing items to process")
public class HelloRequest {

    @JsonProperty("request")
    @NotNull(message = "Request cannot be null")
    @NotEmpty(message = "Request cannot be empty")
    @Valid
    @Schema(description = "Items to include in the greeting")
    private List<RequestItemRecord> request;

    public HelloRequest() {}

    public HelloRequest(List<RequestItemRecord> request) {
        this.request = request;
    }

    public List<RequestItemRecord> getRequest() {
        return request;
    }

    public void setRequest(List<RequestItemRecord> request) {
        this.request = request;
    }
}
