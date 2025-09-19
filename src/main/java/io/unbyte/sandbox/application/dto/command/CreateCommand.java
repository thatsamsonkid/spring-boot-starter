package io.unbyte.sandbox.application.dto.command;

/**
 * Command DTO for application layer using Java Record
 * Represents a business operation to be executed
 * 
 * Records provide:
 * - Immutable data carrier
 * - Automatic equals(), hashCode(), toString()
 * - Compact syntax
 * - Constructor, getters, and accessors
 */
public record CreateCommand(
    String name,
    String description
) {
    
    /**
     * Compact constructor for validation
     */
    public CreateCommand {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (description == null) {
            description = ""; // Default empty string
        }
    }
    
    /**
     * Convenience method for business logic
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
}
