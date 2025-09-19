package io.unbyte.sandbox.infrastructure.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Example request DTO for web layer using Java Record
 * 
 * Note: Records work with validation annotations, but you need to apply them
 * to the record components, not as field annotations
 */
public record CreateRequest(
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    String name,
    
    String description
) {
    
    /**
     * Compact constructor for validation and defaults
     */
    public CreateRequest {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Name must be 255 characters or less");
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
