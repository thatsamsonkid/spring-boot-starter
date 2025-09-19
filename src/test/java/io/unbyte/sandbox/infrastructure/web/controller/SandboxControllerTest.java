package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Integration tests for SandboxController
 */
@SpringBootTest
@AutoConfigureWebTestClient
class SandboxControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void helloEndpoint_shouldReturnHelloResponse() {
        // Create test request
        RequestItemRecord item1 = new RequestItemRecord("3123");
        RequestItemRecord item2 = new RequestItemRecord("4567");
        HelloRequest request = new HelloRequest(Arrays.asList(item1, item2));
        
        webTestClient
            .post()
            .uri("/api/v1/hello")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(HelloResponse.class)
            .value(response -> {
                assert response.message().contains("Hello from Sandbox Service");
                assert response.message().contains("3123");
                assert response.message().contains("4567");
                assert response.service().equals("sandbox-service");
                assert response.timestamp() != null;
            });
    }

    @Test
    void healthEndpoint_shouldReturnHealthResponse() {
        webTestClient
            .get()
            .uri("/api/v1/health")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(HealthResponse.class)
            .value(response -> {
                assert response.status().equals("UP");
                assert response.service().equals("sandbox-service");
                assert response.version().equals("1.0.0");
                assert response.timestamp() != null;
            });
    }
}
