package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.application.command.ProcessHelloCommand;
import io.unbyte.sandbox.application.command.TestErrorCommand;
import io.unbyte.sandbox.application.usecase.GetHealthStatusUseCase;
import io.unbyte.sandbox.application.usecase.ProcessHelloRequestUseCase;
import io.unbyte.sandbox.application.usecase.TestErrorUseCase;
import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import io.unbyte.sandbox.infrastructure.web.service.ErrorTrackingService;
import io.unbyte.sandbox.infrastructure.web.service.PerformanceMonitoringService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


/**
 * REST Controller for Sandbox application
 * Provides basic endpoints for testing and health monitoring
 */
@RestController
@RequestMapping("/api/v1")
public class SandboxController {

    private static final Logger logger = LoggerFactory.getLogger(SandboxController.class);
    
    private final PerformanceMonitoringService performanceMonitoringService;
    private final ErrorTrackingService errorTrackingService;
    private final ProcessHelloRequestUseCase processHelloRequestUseCase;
    private final GetHealthStatusUseCase getHealthStatusUseCase;
    private final TestErrorUseCase testErrorUseCase;

    public SandboxController(PerformanceMonitoringService performanceMonitoringService, 
                           ErrorTrackingService errorTrackingService,
                           ProcessHelloRequestUseCase processHelloRequestUseCase,
                           GetHealthStatusUseCase getHealthStatusUseCase,
                           TestErrorUseCase testErrorUseCase) {
        this.performanceMonitoringService = performanceMonitoringService;
        this.errorTrackingService = errorTrackingService;
        this.processHelloRequestUseCase = processHelloRequestUseCase;
        this.getHealthStatusUseCase = getHealthStatusUseCase;
        this.testErrorUseCase = testErrorUseCase;
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
                
                // Convert DTO to Command
                java.util.List<String> itemIds = request.getRequest().stream()
                        .map(RequestItemRecord::id)
                        .toList();
                
                ProcessHelloCommand command = new ProcessHelloCommand(itemIds);
                
                // Use the use case to process the business logic
                String message = processHelloRequestUseCase.processHelloRequest(command);
                String timestamp = processHelloRequestUseCase.getCurrentTimestamp();
                String serviceName = processHelloRequestUseCase.getServiceName();
                
                logger.info("Hello response created successfully");
                
                return new HelloResponse(message, timestamp, serviceName);
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
                
                // Use the use case to get health status
                String status = getHealthStatusUseCase.getHealthStatus();
                String timestamp = getHealthStatusUseCase.getCurrentTimestamp();
                String serviceName = getHealthStatusUseCase.getServiceName();
                String serviceVersion = getHealthStatusUseCase.getServiceVersion();
                
                HealthResponse response = new HealthResponse(status, timestamp, serviceName, serviceVersion);
                
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
                
                // Convert to Command
                TestErrorCommand command = new TestErrorCommand(errorType);
                
                // Use the use case to test the error
                testErrorUseCase.testError(command);
                
                // This line will never be reached as testError always throws an exception
                return null;
            })
        );
    }
}
