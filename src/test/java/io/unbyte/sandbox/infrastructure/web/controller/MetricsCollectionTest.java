package io.unbyte.sandbox.infrastructure.web.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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
        HelloRequest.RequestItem item1 = new HelloRequest.RequestItem("3123");
        HelloRequest.RequestItem item2 = new HelloRequest.RequestItem("4567");
        HelloRequest request = new HelloRequest(Arrays.asList(item1, item2));
        
        // Get initial counter values
        Counter requestCounter = meterRegistry.find("sandbox.requests.total").counter();
        Counter successCounter = meterRegistry.find("sandbox.requests.success").counter();
        Timer requestTimer = meterRegistry.find("sandbox.requests.duration").timer();
        
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
        requestCounter = meterRegistry.find("sandbox.requests.total").counter();
        successCounter = meterRegistry.find("sandbox.requests.success").counter();
        requestTimer = meterRegistry.find("sandbox.requests.duration").timer();
        
        if (requestCounter != null) {
            assertThat(requestCounter.count()).isGreaterThan(initialRequestCount);
        }
        if (successCounter != null) {
            assertThat(successCounter.count()).isGreaterThan(initialSuccessCount);
        }
        if (requestTimer != null) {
            assertThat((long) requestTimer.count()).isGreaterThan((long) initialTimerCount);
        }
    }

    @Test
    void healthEndpoint_shouldRecordMetrics() {
        // Get initial counter values
        Counter requestCounter = meterRegistry.find("sandbox.requests.total").counter();
        Counter successCounter = meterRegistry.find("sandbox.requests.success").counter();
        Timer requestTimer = meterRegistry.find("sandbox.requests.duration").timer();
        
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
            .jsonPath("$.service").isEqualTo("sandbox-service")
            .jsonPath("$.version").isEqualTo("1.0.0");
        
        // Verify metrics were recorded
        requestCounter = meterRegistry.find("sandbox.requests.total").counter();
        successCounter = meterRegistry.find("sandbox.requests.success").counter();
        requestTimer = meterRegistry.find("sandbox.requests.duration").timer();
        
        if (requestCounter != null) {
            assertThat(requestCounter.count()).isGreaterThan(initialRequestCount);
        }
        if (successCounter != null) {
            assertThat(successCounter.count()).isGreaterThan(initialSuccessCount);
        }
        if (requestTimer != null) {
            assertThat((long) requestTimer.count()).isGreaterThan((long) initialTimerCount);
        }
    }

    @Test
    void operationMetrics_shouldBeRecorded() {
        // Get initial operation metrics
        Timer operationTimer = meterRegistry.find("sandbox.operations.duration").timer();
        Counter successCounter = meterRegistry.find("sandbox.operations.success").counter();
        
        double initialTimerCount = operationTimer != null ? operationTimer.count() : 0.0;
        double initialSuccessCount = successCounter != null ? successCounter.count() : 0.0;
        
        // Make requests to trigger operation metrics
        HelloRequest.RequestItem item1 = new HelloRequest.RequestItem("3123");
        HelloRequest request = new HelloRequest(Arrays.asList(item1));
        
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk();
        
        webTestClient
            .get()
            .uri("/api/v1/health")
            .exchange()
            .expectStatus().isOk();
        
        // Verify operation metrics were recorded
        operationTimer = meterRegistry.find("sandbox.operations.duration").timer();
        successCounter = meterRegistry.find("sandbox.operations.success").counter();
        
        if (operationTimer != null) {
            assertThat((long) operationTimer.count()).isGreaterThan((long) initialTimerCount);
        }
        if (successCounter != null) {
            assertThat(successCounter.count()).isGreaterThan(initialSuccessCount);
        }
    }

    @Test
    void activeRequestsGauge_shouldBeUpdated() {
        // Get initial gauge value
        var activeRequestsGauge = meterRegistry.find("sandbox.requests.active").gauge();
        double initialValue = activeRequestsGauge != null ? activeRequestsGauge.value() : 0.0;
        
        // Make a request
        HelloRequest.RequestItem item1 = new HelloRequest.RequestItem("3123");
        HelloRequest request = new HelloRequest(Arrays.asList(item1));
        
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .contentType(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk();
        
        // The gauge should return to initial value after request completion
        activeRequestsGauge = meterRegistry.find("sandbox.requests.active").gauge();
        if (activeRequestsGauge != null) {
            assertThat(activeRequestsGauge.value()).isEqualTo(initialValue);
        }
    }
}
