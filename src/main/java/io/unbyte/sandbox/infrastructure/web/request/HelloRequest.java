package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for /hello endpoint
 * Updated to use RequestItemRecord instead of inner class
 */
public class HelloRequest {
    
    @JsonProperty("request")
    @NotNull(message = "Request cannot be null")
    @NotEmpty(message = "Request cannot be empty")
    @Valid
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
