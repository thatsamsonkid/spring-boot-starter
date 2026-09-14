package io.unbyte.sandbox.application.command;

/**
 * Command for application layer using Java Record
 * Represents a business operation to be executed
 *
 * This is NOT a DTO - it's a framework-agnostic command object
 * that represents the intent to perform a business operation.
 *
 * Records provide:
 * - Immutable data carrier
 * - Automatic equals(), hashCode(), toString()
 * - Compact syntax
 * - Constructor, getters, and accessors
 */
public record CreateCommand(String name, String description) {

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
