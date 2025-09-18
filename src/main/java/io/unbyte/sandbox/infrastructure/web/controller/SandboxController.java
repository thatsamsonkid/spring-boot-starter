package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Hello endpoint - returns sample data
     * @return HelloResponse with greeting message
     */
    @GetMapping("/hello")
    public ResponseEntity<HelloResponse> hello() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        HelloResponse response = new HelloResponse(
            "Hello from Sandbox Service! 🚀",
            timestamp,
            SERVICE_NAME
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint - returns service health status
     * @return HealthResponse with service health information
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        HealthResponse response = new HealthResponse(
            "UP",
            timestamp,
            SERVICE_NAME,
            SERVICE_VERSION
        );
        
        return ResponseEntity.ok(response);
    }
}
