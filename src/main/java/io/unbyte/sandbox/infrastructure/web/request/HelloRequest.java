package io.unbyte.sandbox.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for /hello endpoint
 */
public class HelloRequest {
    
    @JsonProperty("request")
    @NotNull(message = "Request cannot be null")
    @NotEmpty(message = "Request cannot be empty")
    @Valid
    private List<RequestItem> request;

    public HelloRequest() {}

    public HelloRequest(List<RequestItem> request) {
        this.request = request;
    }

    public List<RequestItem> getRequest() {
        return request;
    }

    public void setRequest(List<RequestItem> request) {
        this.request = request;
    }

    /**
     * Inner class for request items
     */
    public static class RequestItem {
        
        @JsonProperty("id")
        @NotNull(message = "ID cannot be null")
        private String id;

        public RequestItem() {}

        public RequestItem(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
