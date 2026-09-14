package io.unbyte.sandbox.infrastructure.web.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for monitoring performance and tracking errors in reactive streams
 * Provides utilities for measuring execution time and tagging errors
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger logger =
            LoggerFactory.getLogger(PerformanceMonitoringService.class);
    private final MeterRegistry meterRegistry;

    public PerformanceMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Monitor a reactive operation with performance metrics
     */
    public <T> Mono<T> monitorOperation(String operationName, Mono<T> operation) {
        return monitorOperation(operationName, operation, null);
    }

    /**
     * Monitor a reactive operation with performance metrics and custom tags
     */
    public <T> Mono<T> monitorOperation(String operationName, Mono<T> operation, Tag[] customTags) {
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        Instant startTime = Instant.now();

        logger.debug(
                "Starting operation: {} with correlation ID: {}", operationName, correlationId);

        return operation
                .doOnSuccess(
                        result -> {
                            Duration duration = Duration.between(startTime, Instant.now());

                            // Record success metrics
                            recordSuccess(operationName, duration, customTags);

                            logger.info(
                                    "Operation completed successfully: {} in {}ms with correlation"
                                            + " ID: {}",
                                    operationName,
                                    duration.toMillis(),
                                    correlationId);
                        })
                .doOnError(
                        throwable -> {
                            Duration duration = Duration.between(startTime, Instant.now());

                            // Record error metrics
                            recordError(operationName, throwable, duration, customTags);

                            logger.error(
                                    "Operation failed: {} in {}ms with correlation ID: {} - Error:"
                                            + " {}",
                                    operationName,
                                    duration.toMillis(),
                                    correlationId,
                                    throwable.getMessage(),
                                    throwable);
                        });
    }

    /**
     * Monitor a function execution with performance metrics
     */
    public <T, R> R monitorFunction(String functionName, T input, Function<T, R> function) {
        return monitorFunction(functionName, input, function, null);
    }

    /**
     * Monitor a function execution with performance metrics and custom tags
     */
    public <T, R> R monitorFunction(
            String functionName, T input, Function<T, R> function, Tag[] customTags) {
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        Instant startTime = Instant.now();

        logger.debug("Starting function: {} with correlation ID: {}", functionName, correlationId);

        try {
            R result = function.apply(input);
            Duration duration = Duration.between(startTime, Instant.now());

            // Record success metrics
            recordSuccess(functionName, duration, customTags);

            logger.info(
                    "Function completed successfully: {} in {}ms with correlation ID: {}",
                    functionName,
                    duration.toMillis(),
                    correlationId);

            return result;
        } catch (Exception throwable) {
            Duration duration = Duration.between(startTime, Instant.now());

            // Record error metrics
            recordError(functionName, throwable, duration, customTags);

            logger.error(
                    "Function failed: {} in {}ms with correlation ID: {} - Error: {}",
                    functionName,
                    duration.toMillis(),
                    correlationId,
                    throwable.getMessage(),
                    throwable);

            throw throwable;
        }
    }

    /**
     * Record successful operation metrics
     */
    private void recordSuccess(String operationName, Duration duration, Tag[] customTags) {
        Timer.Sample sample = Timer.start(meterRegistry);

        Timer.Builder timerBuilder =
                Timer.builder("sandbox.operations.duration")
                        .tag("operation", operationName)
                        .tag("status", "success")
                        .tag("service", "sandbox");

        if (customTags != null) {
            timerBuilder.tags(Arrays.asList(customTags));
        }

        sample.stop(timerBuilder.register(meterRegistry));

        // Record success counter
        Counter.Builder counterBuilder =
                Counter.builder("sandbox.operations.success")
                        .tag("operation", operationName)
                        .tag("service", "sandbox");

        if (customTags != null) {
            counterBuilder.tags(Arrays.asList(customTags));
        }

        counterBuilder.register(meterRegistry).increment();
    }

    /**
     * Record error metrics
     */
    private void recordError(
            String operationName, Throwable throwable, Duration duration, Tag[] customTags) {
        String errorType = throwable.getClass().getSimpleName();
        String errorMessage = throwable.getMessage();

        Timer.Sample sample = Timer.start(meterRegistry);

        Timer.Builder timerBuilder =
                Timer.builder("sandbox.operations.duration")
                        .tag("operation", operationName)
                        .tag("status", "error")
                        .tag("error_type", errorType)
                        .tag("service", "sandbox");

        if (customTags != null) {
            timerBuilder.tags(Arrays.asList(customTags));
        }

        sample.stop(timerBuilder.register(meterRegistry));

        // Record error counter
        Counter.Builder counterBuilder =
                Counter.builder("sandbox.operations.error")
                        .tag("operation", operationName)
                        .tag("error_type", errorType)
                        .tag("service", "sandbox");

        if (customTags != null) {
            counterBuilder.tags(Arrays.asList(customTags));
        }

        counterBuilder.register(meterRegistry).increment();

        // Record error by type
        Counter.builder("sandbox.errors.by.type")
                .tag("error_type", errorType)
                .tag("operation", operationName)
                .tag("service", "sandbox")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Create custom tags for an operation
     */
    public Tag[] createTags(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }

        Tag[] tags = new Tag[keyValuePairs.length / 2];
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            tags[i / 2] = Tag.of(keyValuePairs[i], keyValuePairs[i + 1]);
        }

        return tags;
    }

    /**
     * Get current metrics summary
     */
    public String getMetricsSummary() {
        return String.format("Performance monitoring active for service: sandbox");
    }
}
