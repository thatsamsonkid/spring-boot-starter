package io.unbyte.sandbox.application.dto.query;

import java.util.UUID;

/**
 * Query DTO for application layer using Java Record
 * Represents a data retrieval operation
 */
public record GetByIdQuery(
    UUID id
) {
    
    /**
     * Compact constructor for validation
     */
    public GetByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
}
