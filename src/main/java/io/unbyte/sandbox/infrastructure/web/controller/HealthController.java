package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.application.usecase.GetHealthStatusUseCase;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import io.unbyte.sandbox.infrastructure.web.service.PerformanceMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Core health endpoint kept when sample APIs are removed from a generated project.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final PerformanceMonitoringService performanceMonitoringService;
    private final GetHealthStatusUseCase getHealthStatusUseCase;

    public HealthController(
            PerformanceMonitoringService performanceMonitoringService,
            GetHealthStatusUseCase getHealthStatusUseCase) {
        this.performanceMonitoringService = performanceMonitoringService;
        this.getHealthStatusUseCase = getHealthStatusUseCase;
    }

    /**
     * Health check endpoint - returns service health status
     * @return Mono<HealthResponse> with service health information
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HealthResponse> health() {
        return performanceMonitoringService.monitorOperation(
                "health_endpoint",
                Mono.fromCallable(
                        () -> {
                            String correlationId = MDC.get("correlationId");

                            logger.info(
                                    "Health check requested with correlation ID: {}",
                                    correlationId);

                            String status = getHealthStatusUseCase.getHealthStatus();
                            String timestamp = getHealthStatusUseCase.getCurrentTimestamp();
                            String serviceName = getHealthStatusUseCase.getServiceName();
                            String serviceVersion = getHealthStatusUseCase.getServiceVersion();

                            HealthResponse response =
                                    new HealthResponse(
                                            status, timestamp, serviceName, serviceVersion);

                            logger.info("Health check completed successfully");

                            return response;
                        }));
    }
}
