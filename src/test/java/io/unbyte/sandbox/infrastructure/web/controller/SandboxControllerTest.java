package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

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
        webTestClient
            .get()
            .uri("/api/v1/hello")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(HelloResponse.class)
            .value(response -> {
                assert response.getMessage().contains("Hello from Sandbox Service");
                assert response.getService().equals("sandbox-service");
                assert response.getTimestamp() != null;
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
                assert response.getStatus().equals("UP");
                assert response.getService().equals("sandbox-service");
                assert response.getVersion().equals("1.0.0");
                assert response.getTimestamp() != null;
            });
    }
}
