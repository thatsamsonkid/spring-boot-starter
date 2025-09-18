package io.unbyte.sandbox.infrastructure.web.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for metrics collection using Micrometer
 * Provides custom meters for service performance monitoring and error tracking
 */
@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final AtomicLong activeRequests = new AtomicLong(0);

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Counter for tracking total requests
     */
    @Bean
    public Counter requestCounter() {
        return Counter.builder("sandbox.requests.total")
                .description("Total number of requests processed")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Counter for tracking successful requests
     */
    @Bean
    public Counter successCounter() {
        return Counter.builder("sandbox.requests.success")
                .description("Total number of successful requests")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Counter for tracking failed requests
     */
    @Bean
    public Counter errorCounter() {
        return Counter.builder("sandbox.requests.error")
                .description("Total number of failed requests")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Timer for measuring request duration
     */
    @Bean
    public Timer requestTimer() {
        return Timer.builder("sandbox.requests.duration")
                .description("Request processing duration")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Timer for measuring hello endpoint duration
     */
    @Bean
    public Timer helloEndpointTimer() {
        return Timer.builder("sandbox.endpoints.hello.duration")
                .description("Hello endpoint processing duration")
                .tag("endpoint", "hello")
                .tag("method", "POST")
                .register(meterRegistry);
    }

    /**
     * Timer for measuring health endpoint duration
     */
    @Bean
    public Timer healthEndpointTimer() {
        return Timer.builder("sandbox.endpoints.health.duration")
                .description("Health endpoint processing duration")
                .tag("endpoint", "health")
                .tag("method", "GET")
                .register(meterRegistry);
    }

    /**
     * Counter for tracking errors by type
     */
    @Bean
    public Counter errorByTypeCounter() {
        return Counter.builder("sandbox.errors.by.type")
                .description("Errors by exception type")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Counter for tracking errors by endpoint
     */
    @Bean
    public Counter errorByEndpointCounter() {
        return Counter.builder("sandbox.errors.by.endpoint")
                .description("Errors by endpoint")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Gauge for tracking active requests
     */
    @Bean
    public Gauge activeRequestsGauge() {
        return Gauge.builder("sandbox.requests.active", activeRequests, AtomicLong::get)
                .description("Number of active requests")
                .tag("service", "sandbox")
                .register(meterRegistry);
    }

    /**
     * Get the active requests counter
     */
    public AtomicLong getActiveRequests() {
        return activeRequests;
    }

    /**
     * Create a timer with custom tags
     */
    public Timer createTimer(String name, String description, Tag... tags) {
        return Timer.builder(name)
                .description(description)
                .tags(Arrays.asList(tags))
                .register(meterRegistry);
    }

    /**
     * Create a counter with custom tags
     */
    public Counter createCounter(String name, String description, Tag... tags) {
        return Counter.builder(name)
                .description(description)
                .tags(Arrays.asList(tags))
                .register(meterRegistry);
    }

    /**
     * Record an error with specific tags
     */
    public void recordError(String endpoint, String errorType, String errorMessage) {
        errorCounter().increment();
        Counter.builder("sandbox.errors.by.type")
            .tag("error_type", errorType)
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
        Counter.builder("sandbox.errors.by.endpoint")
            .tag("endpoint", endpoint)
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record request metrics
     */
    public void recordRequest(String endpoint, String method, boolean success) {
        Counter.builder("sandbox.requests.total")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .tag("service", "sandbox")
            .register(meterRegistry)
            .increment();
        
        if (success) {
            Counter.builder("sandbox.requests.success")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("service", "sandbox")
                .register(meterRegistry)
                .increment();
        } else {
            Counter.builder("sandbox.requests.error")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("service", "sandbox")
                .register(meterRegistry)
                .increment();
        }
    }
}
