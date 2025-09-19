package io.unbyte.sandbox.infrastructure.web.adapter;

import io.unbyte.sandbox.application.port.ExternalApiPort;
import io.unbyte.sandbox.domain.model.Comment;
import io.unbyte.sandbox.infrastructure.web.response.ExternalCommentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Adapter implementation for external API calls
 * Implements the ExternalApiPort to fetch comments from JSONPlaceholder
 */
@Component
public class ExternalApiAdapter implements ExternalApiPort {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiAdapter.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    
    private final WebClient webClient;
    
    public ExternalApiAdapter() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }
    
    @Override
    public Mono<List<Comment>> fetchCommentsByPostId(String postId) {
        logger.info("Fetching comments for post ID: {}", postId);
        
        return webClient.get()
                .uri("/comments?postId={postId}", postId)
                .retrieve()
                .bodyToFlux(ExternalCommentDto.class)
                .map(this::mapToDomainComment)
                .collectList()
                .doOnSuccess(comments -> logger.info("Successfully fetched {} comments for post ID: {}", comments.size(), postId))
                .doOnError(error -> logger.error("Failed to fetch comments for post ID: {}", postId, error));
    }
    
    /**
     * Map external API DTO to domain Comment
     * @param externalComment external API comment DTO
     * @return domain Comment
     */
    private Comment mapToDomainComment(ExternalCommentDto externalComment) {
        return new Comment(
            externalComment.id(),
            externalComment.name(),
            externalComment.email(),
            externalComment.body()
        );
    }
}
