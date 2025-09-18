package io.unbyte.sandbox.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for /health endpoint
 */
public class HealthResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("version")
    private String version;

    public HealthResponse() {}

    public HealthResponse(String status, String timestamp, String service, String version) {
        this.status = status;
        this.timestamp = timestamp;
        this.service = service;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
