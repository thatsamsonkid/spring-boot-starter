package io.unbyte.sandbox.application.dto.command;

/**
 * Command DTO for application layer
 * Represents a business operation to be executed
 */
public class CreateCommand {
    private String name;
    private String description;

    public CreateCommand() {}

    public CreateCommand(String name, String description) {
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
