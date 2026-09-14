package io.unbyte.sandbox.infrastructure.web.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.unbyte.sandbox.infrastructure.web.config.MetricsConfig;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web filter for monitoring request/response metrics
 * Tracks performance, errors, and request patterns
 */
@Component
public class MonitoringFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringFilter.class);
    private final MeterRegistry meterRegistry;
    private final MetricsConfig metricsConfig;

    public MonitoringFilter(MeterRegistry meterRegistry, MetricsConfig metricsConfig) {
        this.meterRegistry = meterRegistry;
        this.metricsConfig = metricsConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String endpoint = getEndpoint(request);
        String method = request.getMethod().name();
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        Instant startTime = Instant.now();

        // Increment active requests
        metricsConfig.getActiveRequests().incrementAndGet();

        logger.info(
                "Processing request: {} {} with correlation ID: {}",
                method,
                endpoint,
                correlationId);

        return chain.filter(exchange)
                .doOnSuccess(
                        aVoid -> {
                            // Check if the response indicates an error based on status code
                            boolean isError =
                                    response.getStatusCode() != null
                                            && response.getStatusCode().isError();

                            Duration duration = Duration.between(startTime, Instant.now());

                            Timer.Sample sample = Timer.start(meterRegistry);
                            sample.stop(
                                    Timer.builder("sandbox.requests.duration")
                                            .tag("endpoint", endpoint)
                                            .tag("method", method)
                                            .tag("status", isError ? "error" : "success")
                                            .tag("service", "sandbox")
                                            .register(meterRegistry));

                            if (isError) {
                                // Record failed request
                                metricsConfig.recordRequest(endpoint, method, false);
                                metricsConfig.recordError(
                                        endpoint,
                                        "HttpError",
                                        "HTTP " + response.getStatusCode().value());

                                logger.error(
                                        "Request failed with HTTP error: {} {} in {}ms with"
                                                + " correlation ID: {} - Status: {}",
                                        method,
                                        endpoint,
                                        duration.toMillis(),
                                        correlationId,
                                        response.getStatusCode());
                            } else {
                                // Record successful request
                                metricsConfig.recordRequest(endpoint, method, true);

                                logger.info(
                                        "Request completed successfully: {} {} in {}ms with"
                                                + " correlation ID: {}",
                                        method,
                                        endpoint,
                                        duration.toMillis(),
                                        correlationId);
                            }
                        })
                .doOnError(
                        throwable -> {
                            // Record failed request
                            Duration duration = Duration.between(startTime, Instant.now());

                            String errorType = throwable.getClass().getSimpleName();
                            String errorMessage = throwable.getMessage();

                            Timer.Sample sample = Timer.start(meterRegistry);
                            sample.stop(
                                    Timer.builder("sandbox.requests.duration")
                                            .tag("endpoint", endpoint)
                                            .tag("method", method)
                                            .tag("status", "error")
                                            .tag("error_type", errorType)
                                            .tag("service", "sandbox")
                                            .register(meterRegistry));

                            metricsConfig.recordRequest(endpoint, method, false);
                            metricsConfig.recordError(endpoint, errorType, errorMessage);

                            logger.error(
                                    "Request failed: {} {} in {}ms with correlation ID: {} - Error:"
                                            + " {}",
                                    method,
                                    endpoint,
                                    duration.toMillis(),
                                    correlationId,
                                    errorMessage,
                                    throwable);
                        })
                .doFinally(
                        signalType -> {
                            // Decrement active requests
                            metricsConfig.getActiveRequests().decrementAndGet();

                            logger.debug(
                                    "Request processing completed: {} {} with signal type: {}",
                                    method,
                                    endpoint,
                                    signalType);
                        });
    }

    /**
     * Extract endpoint name from request path
     */
    private String getEndpoint(ServerHttpRequest request) {
        String path = request.getPath().value();

        // Normalize endpoint names
        if (path.startsWith("/api/v1/hello")) {
            return "hello";
        } else if (path.startsWith("/api/v1/health")) {
            return "health";
        } else if (path.startsWith("/actuator")) {
            return "actuator";
        } else {
            return "unknown";
        }
    }

    @Override
    public int getOrder() {
        // Run after ContextFilter but before other filters
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
