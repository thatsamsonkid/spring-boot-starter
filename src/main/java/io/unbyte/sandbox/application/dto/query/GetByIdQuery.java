package io.unbyte.sandbox.application.dto.query;

import java.util.UUID;

/**
 * Query DTO for application layer
 * Represents a data retrieval operation
 */
public class GetByIdQuery {
    private UUID id;

    public GetByIdQuery() {}

    public GetByIdQuery(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
