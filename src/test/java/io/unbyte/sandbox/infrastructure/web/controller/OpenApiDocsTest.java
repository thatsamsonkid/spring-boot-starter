package io.unbyte.sandbox.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Integration tests for generated OpenAPI docs and Swagger UI. */
@SpringBootTest
@AutoConfigureWebTestClient
class OpenApiDocsTest {

    @Autowired private WebTestClient webTestClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @Test
    void apiDocs_shouldExposeOpenApiSchemaForHealth() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.openapi")
                .value(version -> assertTrue(((String) version).startsWith("3.")))
                .jsonPath("$.info.title")
                .isEqualTo(applicationName + " API")
                .jsonPath("$.paths['/api/v1/health']")
                .exists()
                .jsonPath("$.paths['/api/v1/health'].get.summary")
                .isEqualTo("Get service health")
                .jsonPath("$.components.schemas.HealthResponse")
                .exists();
    }

    @Test
    void apiDocsYaml_shouldContainHealthPath() {
        webTestClient
                .get()
                .uri("/v3/api-docs.yaml")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(
                        body -> {
                            assertTrue(body.contains("openapi:"));
                            assertTrue(body.contains("/api/v1/health"));
                        });
    }

    @Test
    void swaggerUi_shouldBeAvailable() {
        webTestClient
                .get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus()
                .value(
                        status ->
                                assertTrue(
                                        HttpStatus.valueOf(status).is2xxSuccessful()
                                                || HttpStatus.valueOf(status).is3xxRedirection()));
    }

    @Test
    void swaggerConfig_shouldPointAtGeneratedApiDocs() {
        webTestClient
                .get()
                .uri("/v3/api-docs/swagger-config")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.url")
                .isEqualTo("/v3/api-docs");
    }
}
