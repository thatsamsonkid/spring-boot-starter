package io.unbyte.sandbox.application.port;

import io.unbyte.sandbox.domain.model.Comment;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port interface for external API calls
 * Defines the contract for fetching comments from external services
 * 
 * This interface uses reactive types to maintain non-blocking behavior.
 * The implementation in the infrastructure layer handles the reactive concerns.
 */
public interface ExternalApiPort {
    
    /**
     * Fetch comments for a specific post ID
     * @param postId the post ID to fetch comments for
     * @return Mono containing the list of comments (non-blocking)
     */
    Mono<List<Comment>> fetchCommentsByPostId(String postId);
}
