package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.domain.exception.DomainException;
import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import io.unbyte.sandbox.infrastructure.web.service.ErrorTrackingService;
import io.unbyte.sandbox.infrastructure.web.service.PerformanceMonitoringService;
import io.unbyte.sandbox.shared.exception.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger logger = LoggerFactory.getLogger(SandboxController.class);
    private static final String SERVICE_NAME = "sandbox-service";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    private final PerformanceMonitoringService performanceMonitoringService;
    private final ErrorTrackingService errorTrackingService;

    public SandboxController(PerformanceMonitoringService performanceMonitoringService, 
                           ErrorTrackingService errorTrackingService) {
        this.performanceMonitoringService = performanceMonitoringService;
        this.errorTrackingService = errorTrackingService;
    }

    /**
     * Hello endpoint - accepts POST request with request items
     * @param request the request containing items with IDs
     * @return Mono<HelloResponse> with greeting message and processed IDs
     */
    @PostMapping(value = "/hello", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HelloResponse> hello(@Valid @RequestBody HelloRequest request) {
        return performanceMonitoringService.monitorOperation("hello_endpoint", 
            Mono.fromCallable(() -> {
                // Context is automatically available in MDC due to the MDC bridge
                String correlationId = MDC.get("correlationId");
                String userId = MDC.get("userId");
                
                logger.info("Processing hello request with correlation ID: {}", correlationId);
                logger.info("Processing request for user: {}", userId);
                
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                
                // Process the request items
                String processedIds = request.getRequest().stream()
                        .map(RequestItemRecord::id)
                        .reduce((id1, id2) -> id1 + ", " + id2)
                        .orElse("none");
                
                String message = String.format("Hello from Sandbox Service! 🚀 Processed IDs: [%s]", processedIds);
                
                logger.info("Hello response created successfully");
                
                return new HelloResponse(
                    message,
                    timestamp,
                    SERVICE_NAME
                );
            })
        );
    }

    /**
     * Health check endpoint - returns service health status
     * @return Mono<HealthResponse> with service health information
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HealthResponse> health() {
        return performanceMonitoringService.monitorOperation("health_endpoint",
            Mono.fromCallable(() -> {
                // Context is automatically available in MDC due to the MDC bridge
                String correlationId = MDC.get("correlationId");
                
                logger.info("Health check requested with correlation ID: {}", correlationId);
                
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                
                HealthResponse response = new HealthResponse(
                    "UP",
                    timestamp,
                    SERVICE_NAME,
                    SERVICE_VERSION
                );
                
                logger.info("Health check completed successfully");
                
                return response;
            })
        );
    }

    /**
     * Test endpoint to demonstrate error handling
     * @param errorType the type of error to simulate
     * @return Mono<HelloResponse> or throws exception
     */
    @GetMapping(value = "/test-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HelloResponse> testError(@RequestParam String errorType) {
        return performanceMonitoringService.monitorOperation("test_error_endpoint",
            Mono.fromCallable(() -> {
                String correlationId = MDC.get("correlationId");
                String userId = MDC.get("userId");
                
                logger.info("Testing error type: {} with correlation ID: {}", errorType, correlationId);
                
                switch (errorType.toLowerCase()) {
                    case "domain_validation":
                        throw DomainException.validationFailed("testField", "Test validation error");
                    case "domain_entity_not_found":
                        throw DomainException.entityNotFound("TestEntity", "test-id-123");
                    case "domain_business_rule":
                        throw DomainException.businessRuleViolation("TestRule", "Business rule violation");
                    case "application_use_case":
                        throw new io.unbyte.sandbox.application.exception.ApplicationServiceException(
                            ErrorCode.APPLICATION_USE_CASE_FAILED,
                            "Test use case failed"
                        );
                    case "infrastructure_database":
                        throw new io.unbyte.sandbox.application.exception.ApplicationServiceException(
                            ErrorCode.INFRASTRUCTURE_DATABASE_ERROR,
                            "Test database error"
                        );
                    case "system_internal":
                        throw new RuntimeException("Test system error");
                    default:
                        throw new IllegalArgumentException("Unknown error type: " + errorType);
                }
            })
        );
    }
}
