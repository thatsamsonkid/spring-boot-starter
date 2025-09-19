package io.unbyte.sandbox.application.usecase;

import io.unbyte.sandbox.application.exception.ApplicationServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Use case for getting health status
 * Contains the business logic for health checks
 */
@Component
public class GetHealthStatusUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(GetHealthStatusUseCase.class);
    private static final String SERVICE_NAME = "sandbox-service";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * Get the health status
     * @return the health status
     */
    public String getHealthStatus() {
        logger.info("Getting health status");
        
        try {
            // Business logic: Check service health
            String status = checkServiceHealth();
            
            logger.info("Health status retrieved successfully: {}", status);
            return status;
            
        } catch (Exception e) {
            logger.error("Failed to get health status", e);
            throw ApplicationServiceException.useCaseFailed("GetHealthStatus", e.getMessage());
        }
    }
    
    /**
     * Check the service health
     * @return the health status
     */
    private String checkServiceHealth() {
        // Business logic: In a real application, this would check:
        // - Database connectivity
        // - External service availability
        // - System resources
        // - etc.
        
        // For now, we'll always return "UP" as this is a demo
        return "UP";
    }
    
    /**
     * Get the current timestamp
     * @return formatted timestamp
     */
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
    
    /**
     * Get the service name
     * @return service name
     */
    public String getServiceName() {
        return SERVICE_NAME;
    }
    
    /**
     * Get the service version
     * @return service version
     */
    public String getServiceVersion() {
        return SERVICE_VERSION;
    }
}
