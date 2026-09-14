package io.unbyte.sandbox.infrastructure.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.unbyte.sandbox.infrastructure.web.response.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Integration tests for the core health endpoint. */
@SpringBootTest
@AutoConfigureWebTestClient
class HealthControllerTest {

    @Autowired private WebTestClient webTestClient;

    @Test
    void healthEndpoint_shouldReturnHealthResponse() {
        webTestClient
                .get()
                .uri("/api/v1/health")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody(HealthResponse.class)
                .value(
                        response -> {
                            assert response.status().equals("UP");
                            assert response.service().equals("sandbox-service");
                            assert response.version().equals("1.0.0");
                            assert response.timestamp() != null;
                        });
    }
}
