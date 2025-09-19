package io.unbyte.sandbox.application.usecase;

import io.unbyte.sandbox.application.command.ProcessHelloCommand;
import io.unbyte.sandbox.application.exception.ApplicationServiceException;
import io.unbyte.sandbox.shared.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Use case for processing hello requests
 * Contains the business logic for processing hello requests
 * 
 * This class is framework-agnostic and should not have Spring annotations.
 * Dependency injection is handled by the infrastructure layer.
 */
public class ProcessHelloRequestUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessHelloRequestUseCase.class);
    private static final String SERVICE_NAME = "sandbox-service";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * Process a hello request
     * @param command the command containing item IDs to process
     * @return the processed message
     */
    public String processHelloRequest(ProcessHelloCommand command) {
        logger.info("Processing hello request with {} items", command.getItemCount());
        
        try {
            // Business logic: Process the request items
            String processedIds = processItemIds(command.itemIds());
            
            // Business logic: Create the response message
            String message = createHelloMessage(processedIds);
            
            logger.info("Hello request processed successfully");
            return message;
            
        } catch (Exception e) {
            logger.error("Failed to process hello request", e);
            throw ApplicationServiceException.useCaseFailed("ProcessHelloRequest", e.getMessage());
        }
    }
    
    /**
     * Process the item IDs into a formatted string
     * @param itemIds the list of item IDs
     * @return formatted string of processed IDs
     */
    private String processItemIds(java.util.List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return "none";
        }
        
        return itemIds.stream()
                .reduce((id1, id2) -> id1 + ", " + id2)
                .orElse("none");
    }
    
    /**
     * Create the hello message
     * @param processedIds the processed IDs
     * @return the hello message
     */
    private String createHelloMessage(String processedIds) {
        return String.format("Hello from Sandbox Service! 🚀 Processed IDs: [%s]", processedIds);
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
}
