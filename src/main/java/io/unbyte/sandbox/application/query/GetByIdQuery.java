package io.unbyte.sandbox.application.query;

import java.util.UUID;

/**
 * Query for application layer using Java Record
 * Represents a data retrieval operation
 *
 * This is NOT a DTO - it's a framework-agnostic query object
 * that represents the intent to retrieve data.
 */
public record GetByIdQuery(UUID id) {

    /**
     * Compact constructor for validation
     */
    public GetByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
}
