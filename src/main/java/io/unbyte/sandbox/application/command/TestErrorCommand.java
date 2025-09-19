package io.unbyte.sandbox.application.command;

/**
 * Command for testing error scenarios using Java Record
 * Represents the intent to test a specific error type
 * 
 * This is NOT a DTO - it's a framework-agnostic command object
 * that represents the intent to test error scenarios.
 */
public record TestErrorCommand(
    String errorType
) {
    
    /**
     * Compact constructor for validation
     */
    public TestErrorCommand {
        if (errorType == null || errorType.trim().isEmpty()) {
            throw new IllegalArgumentException("Error type cannot be null or empty");
        }
    }
    
    /**
     * Get the error type in lowercase for consistent comparison
     */
    public String getErrorTypeLowercase() {
        return errorType.toLowerCase();
    }
    
    /**
     * Check if this is a domain error
     */
    public boolean isDomainError() {
        return errorType.toLowerCase().startsWith("domain_");
    }
    
    /**
     * Check if this is an application error
     */
    public boolean isApplicationError() {
        return errorType.toLowerCase().startsWith("application_");
    }
    
    /**
     * Check if this is an infrastructure error
     */
    public boolean isInfrastructureError() {
        return errorType.toLowerCase().startsWith("infrastructure_");
    }
}
