package io.unbyte.sandbox.infrastructure.web.response;

import io.unbyte.sandbox.infrastructure.web.WebDto;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Example response DTO for web layer
 */
public class Response extends WebDto {

    private String name;
    private String description;

    public Response() {}

    public Response(
            UUID id,
            String name,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
