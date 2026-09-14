package io.unbyte.sandbox.application.command;

import java.util.List;

/**
 * Command for processing hello request using Java Record
 * Represents the intent to process a hello request with items
 *
 * This is NOT a DTO - it's a framework-agnostic command object
 * that represents the intent to process a hello request.
 */
public record ProcessHelloCommand(List<String> itemIds) {

    /**
     * Compact constructor for validation
     */
    public ProcessHelloCommand {
        if (itemIds == null) {
            throw new IllegalArgumentException("Item IDs cannot be null");
        }
    }

    /**
     * Convenience method for business logic
     */
    public boolean hasItems() {
        return itemIds != null && !itemIds.isEmpty();
    }

    /**
     * Get the number of items
     */
    public int getItemCount() {
        return itemIds != null ? itemIds.size() : 0;
    }
}
