package io.unbyte.sandbox.infrastructure.web.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.unbyte.sandbox.shared.exception.ApplicationException;
import io.unbyte.sandbox.shared.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for tracking and classifying errors
 * Integrates with metrics and logging systems
 */
@Service
public class ErrorTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorTrackingService.class);
    private final MeterRegistry meterRegistry;

    public ErrorTrackingService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Track an error with full context
     */
    public void trackError(ApplicationException exception) {
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");
        
        // Log structured error information
        logger.error("Error tracked: {} - Layer: {} - Operation: {} - Correlation ID: {} - User ID: {}", 
            exception.getErrorCode(), 
            exception.getLayer(), 
            exception.getOperation(),
            correlationId, 
            userId, 
            exception);
        
        // Record metrics
        recordErrorMetrics(exception);
        
        // Record error details
        recordErrorDetails(exception, correlationId, userId);
    }

    /**
     * Track a generic error
     */
    public void trackError(Throwable throwable, String layer, String operation) {
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");
        
        logger.error("Generic error tracked: {} - Layer: {} - Operation: {} - Correlation ID: {} - User ID: {}", 
            throwable.getClass().getSimpleName(), 
            layer, 
            operation,
            correlationId, 
            userId, 
            throwable);
        
        // Record metrics for generic errors
        Counter.builder("sandbox.errors.generic")
            .tag("layer", layer)
            .tag("operation", operation)
            .tag("error_type", throwable.getClass().getSimpleName())
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record error metrics
     */
    private void recordErrorMetrics(ApplicationException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        
        // Record error by code
        Counter.builder("sandbox.errors.by.code")
            .tag("error_code", errorCode.getCode())
            .tag("layer", errorCode.getLayer())
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
        
        // Record error by layer
        Counter.builder("sandbox.errors.by.layer")
            .tag("layer", errorCode.getLayer())
            .tag("operation", exception.getOperation() != null ? exception.getOperation() : "unknown")
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
        
        // Record error by type
        Counter.builder("sandbox.errors.by.type")
            .tag("error_type", errorCode.name())
            .tag("layer", errorCode.getLayer())
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record detailed error information
     */
    private void recordErrorDetails(ApplicationException exception, String correlationId, String userId) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", exception.getErrorCode().getCode());
        errorDetails.put("layer", exception.getLayer());
        errorDetails.put("operation", exception.getOperation());
        errorDetails.put("correlationId", correlationId);
        errorDetails.put("userId", userId);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("context", exception.getContext());
        
        // This could be extended to send to external monitoring systems
        logger.info("Error details recorded: {}", errorDetails);
    }

    /**
     * Get error statistics
     */
    public Map<String, Object> getErrorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // This could be extended to query metrics registry for statistics
        stats.put("timestamp", LocalDateTime.now());
        stats.put("service", "sandbox");
        
        return stats;
    }
}

