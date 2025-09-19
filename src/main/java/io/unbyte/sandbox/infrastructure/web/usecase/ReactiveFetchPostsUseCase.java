package io.unbyte.sandbox.infrastructure.web.usecase;

import io.unbyte.sandbox.application.port.ExternalApiPort;
import io.unbyte.sandbox.domain.model.Comment;
import io.unbyte.sandbox.domain.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive use case for fetching posts with comments
 * 
 * This class handles the reactive concerns in the infrastructure layer.
 * It directly uses the reactive port interface for non-blocking behavior.
 */
@Component
public class ReactiveFetchPostsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveFetchPostsUseCase.class);
    private final ExternalApiPort externalApiPort;

    public ReactiveFetchPostsUseCase(ExternalApiPort externalApiPort) {
        this.externalApiPort = externalApiPort;
    }

    /**
     * Fetch posts with comments reactively
     * @param postIds list of post IDs to fetch
     * @return Mono containing list of posts with their comments
     */
    public Mono<List<Post>> fetchPostsWithComments(List<String> postIds) {
        logger.info("Fetching posts with comments reactively for {} post IDs", postIds.size());
        
        if (postIds == null || postIds.isEmpty()) {
            logger.warn("No post IDs provided");
            return Mono.just(List.of());
        }
        
        // Fetch comments for each post ID reactively
        List<Mono<Post>> postMonos = postIds.stream()
                .map(this::fetchPostWithComments)
                .toList();
        
        // Combine all posts into a single list
        return Mono.zip(postMonos, results -> 
                List.of(results)
                        .stream()
                        .map(result -> (Post) result)
                        .toList()
        )
        .doOnSuccess(posts -> logger.info("Successfully fetched {} posts with comments", posts.size()))
        .doOnError(e -> logger.error("Failed to fetch posts with comments", e));
    }
    
    /**
     * Fetch a single post with its comments
     * @param postId the post ID to fetch
     * @return Mono containing the post with comments
     */
    private Mono<Post> fetchPostWithComments(String postId) {
        return externalApiPort.fetchCommentsByPostId(postId)
                .map(comments -> new Post(postId, comments))
                .doOnSuccess(post -> logger.info("Successfully created post {} with {} comments", postId, post.getComments().size()))
                .doOnError(error -> logger.error("Failed to fetch post {} with comments", postId, error));
    }
}
