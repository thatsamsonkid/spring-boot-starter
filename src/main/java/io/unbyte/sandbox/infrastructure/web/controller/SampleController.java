package io.unbyte.sandbox.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.unbyte.sandbox.application.command.FetchPostsCommand;
import io.unbyte.sandbox.application.command.ProcessHelloCommand;
import io.unbyte.sandbox.application.command.TestErrorCommand;
import io.unbyte.sandbox.application.usecase.ProcessHelloRequestUseCase;
import io.unbyte.sandbox.application.usecase.TestErrorUseCase;
import io.unbyte.sandbox.infrastructure.web.mapper.PostMapper;
import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.PostRequestDto;
import io.unbyte.sandbox.infrastructure.web.request.PostsRequestDto;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.PostResponseDto;
import io.unbyte.sandbox.infrastructure.web.response.PostsResponseDto;
import io.unbyte.sandbox.infrastructure.web.service.PerformanceMonitoringService;
import io.unbyte.sandbox.infrastructure.web.usecase.ReactiveFetchPostsUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Optional sample APIs (hello, posts, error demo). Removed when a project is generated without
 * sample code.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sample", description = "Optional sample APIs included with the starter")
public class SampleController {

    private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

    private final PerformanceMonitoringService performanceMonitoringService;
    private final ProcessHelloRequestUseCase processHelloRequestUseCase;
    private final TestErrorUseCase testErrorUseCase;
    private final ReactiveFetchPostsUseCase fetchPostsUseCase;
    private final PostMapper postMapper;

    public SampleController(
            PerformanceMonitoringService performanceMonitoringService,
            ProcessHelloRequestUseCase processHelloRequestUseCase,
            TestErrorUseCase testErrorUseCase,
            ReactiveFetchPostsUseCase fetchPostsUseCase,
            PostMapper postMapper) {
        this.performanceMonitoringService = performanceMonitoringService;
        this.processHelloRequestUseCase = processHelloRequestUseCase;
        this.testErrorUseCase = testErrorUseCase;
        this.fetchPostsUseCase = fetchPostsUseCase;
        this.postMapper = postMapper;
    }

    /**
     * Hello endpoint - accepts POST request with request items
     * @param request the request containing items with IDs
     * @return Mono<HelloResponse> with greeting message and processed IDs
     */
    @Operation(
            summary = "Process hello request",
            description = "Accepts request items and returns a greeting with processed IDs")
    @PostMapping(
            value = "/hello",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HelloResponse> hello(@Valid @RequestBody HelloRequest request) {
        return performanceMonitoringService.monitorOperation(
                "hello_endpoint",
                Mono.fromCallable(
                        () -> {
                            String correlationId = MDC.get("correlationId");
                            String userId = MDC.get("userId");

                            logger.info(
                                    "Processing hello request with correlation ID: {}",
                                    correlationId);
                            logger.info("Processing request for user: {}", userId);

                            java.util.List<String> itemIds =
                                    request.getRequest().stream()
                                            .map(RequestItemRecord::id)
                                            .toList();

                            ProcessHelloCommand command = new ProcessHelloCommand(itemIds);

                            String message =
                                    processHelloRequestUseCase.processHelloRequest(command);
                            String timestamp = processHelloRequestUseCase.getCurrentTimestamp();
                            String serviceName = processHelloRequestUseCase.getServiceName();

                            logger.info("Hello response created successfully");

                            return new HelloResponse(message, timestamp, serviceName);
                        }));
    }

    /**
     * Test endpoint to demonstrate error handling
     * @param errorType the type of error to simulate
     * @return Mono<HelloResponse> or throws exception
     */
    @Operation(
            summary = "Trigger a sample error",
            description = "Simulates an error of the requested type for handler testing")
    @GetMapping(value = "/test-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HelloResponse> testError(
            @Parameter(description = "Error type to simulate", example = "domain_validation")
                    @RequestParam
                    String errorType) {
        return performanceMonitoringService.monitorOperation(
                "test_error_endpoint",
                Mono.fromCallable(
                        () -> {
                            String correlationId = MDC.get("correlationId");

                            logger.info(
                                    "Testing error type: {} with correlation ID: {}",
                                    errorType,
                                    correlationId);

                            TestErrorCommand command = new TestErrorCommand(errorType);
                            testErrorUseCase.testError(command);

                            return null;
                        }));
    }

    /**
     * Fetch posts with comments from external API
     * @param request the request containing post IDs
     * @return Mono<PostsResponseDto> with posts and their comments
     */
    @Operation(
            summary = "Fetch posts with comments",
            description = "Loads posts and their comments from the external sample API")
    @PostMapping(
            value = "/posts",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PostsResponseDto> fetchPosts(@Valid @RequestBody PostsRequestDto request) {
        return performanceMonitoringService.monitorOperation(
                "fetch_posts_endpoint",
                Mono.fromCallable(
                                () -> {
                                    String correlationId = MDC.get("correlationId");
                                    String userId = MDC.get("userId");

                                    logger.info(
                                            "Fetching posts with correlation ID: {}",
                                            correlationId);
                                    logger.info("Processing request for user: {}", userId);

                                    List<String> postIds =
                                            request.getRequest().stream()
                                                    .map(PostRequestDto::postId)
                                                    .toList();

                                    FetchPostsCommand command = new FetchPostsCommand(postIds);

                                    logger.info(
                                            "Created command with {} post IDs",
                                            command.getPostCount());

                                    return command;
                                })
                        .flatMap(
                                command ->
                                        fetchPostsUseCase.fetchPostsWithComments(command.postIds()))
                        .map(
                                posts -> {
                                    List<PostResponseDto> postDtos =
                                            posts.stream().map(postMapper::toResponseDto).toList();

                                    PostsResponseDto.DataDto data =
                                            new PostsResponseDto.DataDto(postDtos);
                                    PostsResponseDto response = new PostsResponseDto(data);

                                    logger.info(
                                            "Successfully fetched {} posts with comments",
                                            posts.size());

                                    return response;
                                }));
    }
}
