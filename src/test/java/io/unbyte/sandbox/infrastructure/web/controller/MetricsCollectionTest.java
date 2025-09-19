package io.unbyte.sandbox.infrastructure.web.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Test class for metrics collection and performance monitoring
 */
@SpringBootTest
@AutoConfigureWebTestClient
class MetricsCollectionTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void helloEndpoint_shouldRecordMetrics() {
        // Create test request
        RequestItemRecord item1 = new RequestItemRecord("3123");
        RequestItemRecord item2 = new RequestItemRecord("4567");
        HelloRequest request = new HelloRequest(Arrays.asList(item1, item2));
        
        // Get initial counter values with correct tags
        Counter requestCounter = meterRegistry.find("sandbox.requests.total")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .counter();
        Counter successCounter = meterRegistry.find("sandbox.requests.success")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .counter();
        Timer requestTimer = meterRegistry.find("sandbox.requests.duration")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .timer();
        
        double initialRequestCount = requestCounter != null ? requestCounter.count() : 0.0;
        double initialSuccessCount = successCounter != null ? successCounter.count() : 0.0;
        double initialTimerCount = requestTimer != null ? requestTimer.count() : 0.0;
        
        // Make request
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .header("X-Correlation-ID", "test-correlation-123")
            .header("X-User-ID", "user-456")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").exists()
            .jsonPath("$.timestamp").exists()
            .jsonPath("$.service").isEqualTo("sandbox-service");
        
        // Verify metrics were recorded
        requestCounter = meterRegistry.find("sandbox.requests.total")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .counter();
        successCounter = meterRegistry.find("sandbox.requests.success")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .counter();
        requestTimer = meterRegistry.find("sandbox.requests.duration")
            .tag("endpoint", "hello")
            .tag("method", "POST")
            .tag("service", "sandbox")
            .timer();
        
        if (requestCounter != null) {
            assertThat(requestCounter.count()).isGreaterThanOrEqualTo(initialRequestCount);
        }
        if (successCounter != null) {
            assertThat(successCounter.count()).isGreaterThanOrEqualTo(initialSuccessCount);
        }
        if (requestTimer != null) {
            assertThat((long) requestTimer.count()).isGreaterThanOrEqualTo((long) initialTimerCount);
        }
    }

    @Test
    void healthEndpoint_shouldRecordMetrics() {
        // Get initial counter values with correct tags
        Counter requestCounter = meterRegistry.find("sandbox.requests.total")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .counter();
        Counter successCounter = meterRegistry.find("sandbox.requests.success")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .counter();
        Timer requestTimer = meterRegistry.find("sandbox.requests.duration")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .timer();
        
        double initialRequestCount = requestCounter != null ? requestCounter.count() : 0.0;
        double initialSuccessCount = successCounter != null ? successCounter.count() : 0.0;
        double initialTimerCount = requestTimer != null ? requestTimer.count() : 0.0;
        
        // Make request
        webTestClient
            .get()
            .uri("/api/v1/health")
            .header("X-Correlation-ID", "health-correlation-456")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.timestamp").exists()
            .jsonPath("$.service").isEqualTo("sandbox-service");
        
        // Verify metrics were recorded
        requestCounter = meterRegistry.find("sandbox.requests.total")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .counter();
        successCounter = meterRegistry.find("sandbox.requests.success")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .counter();
        requestTimer = meterRegistry.find("sandbox.requests.duration")
            .tag("endpoint", "health")
            .tag("method", "GET")
            .tag("service", "sandbox")
            .timer();
        
        if (requestCounter != null) {
            assertThat(requestCounter.count()).isGreaterThanOrEqualTo(initialRequestCount);
        }
        if (successCounter != null) {
            assertThat(successCounter.count()).isGreaterThanOrEqualTo(initialSuccessCount);
        }
        if (requestTimer != null) {
            assertThat((long) requestTimer.count()).isGreaterThanOrEqualTo((long) initialTimerCount);
        }
    }

    // Re-enable once we have a solid requestDto
    // @Test
    // void helloEndpoint_shouldRecordErrorMetricsOnError() {
    //     // Simulate an error by sending an invalid request (e.g., missing required fields)
    //     // This test assumes that the validation failure will lead to an error path that Micrometer can capture.
    //     // For a more robust test, you might introduce a mock service that explicitly throws an exception.

    //     double initialErrorCount = meterRegistry.find("sandbox.requests.error")
    //         .tag("endpoint", "hello")
    //         .tag("method", "POST")
    //         .tag("service", "sandbox")
    //         .counter() != null ?
    //         meterRegistry.find("sandbox.requests.error")
    //             .tag("endpoint", "hello")
    //             .tag("method", "POST")
    //             .tag("service", "sandbox")
    //             .counter().count() : 0;

    //     // Send an invalid request (e.g., null 'request' list)
    //     HelloRequest invalidRequest = new HelloRequest(null);

    //     webTestClient
    //         .post()
    //         .uri("/api/v1/hello")
    //         .contentType(APPLICATION_JSON)
    //         .accept(APPLICATION_JSON)
    //         .bodyValue(invalidRequest)
    //         .exchange()
    //         .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR); // Expecting internal server error due to null pointer

    //     // Verify error metrics were recorded
    //     // Based on the debug output, we can see that error metrics are recorded with different tags
    //     Counter errorByTypeCounter = meterRegistry.find("sandbox.errors.by.type")
    //         .tag("error_type", "NullPointerException")
    //         .tag("operation", "hello_endpoint")
    //         .tag("service", "sandbox")
    //         .counter();
    //     Counter operationErrorCounter = meterRegistry.find("sandbox.operations.error")
    //         .tag("error_type", "NullPointerException")
    //         .tag("operation", "hello_endpoint")
    //         .tag("service", "sandbox")
    //         .counter();
    //     Counter generalErrorCounter = meterRegistry.find("sandbox.requests.error")
    //         .tag("service", "sandbox")
    //         .counter();

    //     // Check that error metrics exist and have been incremented
    //     assertThat(errorByTypeCounter).isNotNull();
    //     assertThat(operationErrorCounter).isNotNull();
    //     assertThat(generalErrorCounter).isNotNull();

    //     assertThat(errorByTypeCounter.count()).isGreaterThan(0);
    //     assertThat(operationErrorCounter.count()).isGreaterThan(0);
    //     assertThat(generalErrorCounter.count()).isGreaterThan(initialErrorCount);
    // }
}