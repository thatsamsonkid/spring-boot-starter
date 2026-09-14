package io.unbyte.sandbox.application.usecase;

import io.unbyte.sandbox.application.exception.ApplicationServiceException;
import io.unbyte.sandbox.application.port.ExternalApiPort;
import io.unbyte.sandbox.domain.model.Post;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use case for fetching posts with comments
 * Orchestrates the business logic for fetching posts from external API
 *
 * This class is framework-agnostic and should not have Spring annotations.
 * Dependency injection is handled by the infrastructure layer.
 */
public class FetchPostsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(FetchPostsUseCase.class);

    private final ExternalApiPort externalApiPort;

    public FetchPostsUseCase(ExternalApiPort externalApiPort) {
        this.externalApiPort = externalApiPort;
    }

    /**
     * Fetch posts with comments for the given post IDs
     * @param postIds list of post IDs to fetch
     * @return list of posts with their comments
     */
    public List<Post> fetchPostsWithComments(List<String> postIds) {
        logger.info("Fetching posts with comments for {} post IDs", postIds.size());

        try {
            if (postIds == null || postIds.isEmpty()) {
                logger.warn("No post IDs provided");
                return List.of();
            }

            // Fetch comments for each post ID
            List<Post> posts = new ArrayList<>();
            for (String postId : postIds) {
                Post post = fetchPostWithComments(postId);
                posts.add(post);
            }

            logger.info("Successfully fetched {} posts with comments", posts.size());
            return posts;

        } catch (Exception e) {
            logger.error("Failed to fetch posts with comments", e);
            throw ApplicationServiceException.useCaseFailed("FetchPosts", e.getMessage());
        }
    }

    /**
     * Fetch a single post with its comments
     * @param postId the post ID to fetch
     * @return the post with comments
     */
    private Post fetchPostWithComments(String postId) {
        try {
            // This will be handled by the reactive wrapper in infrastructure layer
            // For now, we'll create an empty post to maintain the interface
            // The actual reactive implementation is in ReactiveFetchPostsUseCase
            Post post = new Post(postId, List.of());
            logger.info(
                    "Successfully created post {} with {} comments",
                    postId,
                    post.getComments().size());
            return post;
        } catch (Exception error) {
            logger.error("Failed to fetch post {} with comments", postId, error);
            throw error;
        }
    }
}
