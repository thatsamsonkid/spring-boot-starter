package io.unbyte.sandbox.application.command;

import java.util.List;

/**
 * Command for fetching posts using Java Record
 * Represents the intent to fetch posts with comments
 * 
 * This is NOT a DTO - it's a framework-agnostic command object
 * that represents the intent to fetch posts.
 */
public record FetchPostsCommand(
    List<String> postIds
) {
    
    /**
     * Compact constructor for validation
     */
    public FetchPostsCommand {
        if (postIds == null) {
            throw new IllegalArgumentException("Post IDs cannot be null");
        }
    }
    
    /**
     * Get the number of post IDs
     */
    public int getPostCount() {
        return postIds != null ? postIds.size() : 0;
    }
    
    /**
     * Check if there are any post IDs
     */
    public boolean hasPosts() {
        return postIds != null && !postIds.isEmpty();
    }
}
