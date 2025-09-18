package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for Sandbox application
 * Provides basic endpoints for testing and health monitoring
 */
@RestController
@RequestMapping("/api/v1")
public class SandboxController {

    private static final String SERVICE_NAME = "sandbox-service";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Hello endpoint - accepts POST request with request items
     * @param request the request containing items with IDs
     * @return Mono<HelloResponse> with greeting message and processed IDs
     */
    @PostMapping(value = "/hello", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HelloResponse> hello(@Valid @RequestBody HelloRequest request) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        // Process the request items
        String processedIds = request.getRequest().stream()
                .map(HelloRequest.RequestItem::getId)
                .reduce((id1, id2) -> id1 + ", " + id2)
                .orElse("none");
        
        String message = String.format("Hello from Sandbox Service! 🚀 Processed IDs: [%s]", processedIds);
        
        HelloResponse response = new HelloResponse(
            message,
            timestamp,
            SERVICE_NAME
        );
        
        return Mono.just(response);
    }

    /**
     * Health check endpoint - returns service health status
     * @return Mono<HealthResponse> with service health information
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HealthResponse> health() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        HealthResponse response = new HealthResponse(
            "UP",
            timestamp,
            SERVICE_NAME,
            SERVICE_VERSION
        );
        
        return Mono.just(response);
    }
}
