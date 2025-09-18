package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Test class to demonstrate context propagation in reactive streams
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ContextPropagationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void helloEndpoint_shouldPropagateContext() {
        // Create test request
        HelloRequest.RequestItem item1 = new HelloRequest.RequestItem("3123");
        HelloRequest.RequestItem item2 = new HelloRequest.RequestItem("4567");
        HelloRequest request = new HelloRequest(Arrays.asList(item1, item2));
        
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .header("X-Correlation-ID", "test-correlation-123")
            .header("X-User-ID", "user-456")
            .header("X-Trace-ID", "trace-789")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Correlation-ID")
            .expectBody()
            .jsonPath("$.message").exists()
            .jsonPath("$.timestamp").exists()
            .jsonPath("$.service").isEqualTo("sandbox-service");
    }

    @Test
    void healthEndpoint_shouldPropagateContext() {
        webTestClient
            .get()
            .uri("/api/v1/health")
            .header("X-Correlation-ID", "health-correlation-456")
            .header("X-User-ID", "health-user-789")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Correlation-ID")
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.service").isEqualTo("sandbox-service")
            .jsonPath("$.version").isEqualTo("1.0.0");
    }

    @Test
    void helloEndpoint_shouldGenerateCorrelationIdWhenMissing() {
        // Create test request without correlation ID header
        HelloRequest.RequestItem item1 = new HelloRequest.RequestItem("3123");
        HelloRequest request = new HelloRequest(Arrays.asList(item1));
        
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Correlation-ID")
            .expectBody()
            .jsonPath("$.message").exists();
    }
}
