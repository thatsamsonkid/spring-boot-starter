package io.unbyte.sandbox.infrastructure.web.controller;

import io.unbyte.sandbox.infrastructure.web.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Test class for error handling and exception management
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ErrorHandlingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testDomainValidationError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=domain_validation")
            .header("X-Correlation-ID", "test-correlation-123")
            .header("X-User-ID", "user-456")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("DOMAIN_001");
                assert response.getError().getLayer().equals("Domain");
                assert response.getError().getMessage().contains("Validation failed for field testField");
                assert response.getCorrelationId().equals("test-correlation-123");
            });
    }

    @Test
    void testDomainEntityNotFoundError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=domain_entity_not_found")
            .header("X-Correlation-ID", "test-correlation-456")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("DOMAIN_003");
                assert response.getError().getLayer().equals("Domain");
                assert response.getError().getMessage().contains("Entity TestEntity with ID test-id-123 not found");
                assert response.getCorrelationId().equals("test-correlation-456");
            });
    }

    @Test
    void testDomainBusinessRuleError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=domain_business_rule")
            .header("X-Correlation-ID", "test-correlation-789")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("DOMAIN_002");
                assert response.getError().getLayer().equals("Domain");
                assert response.getError().getMessage().contains("Business rule violation");
                assert response.getCorrelationId().equals("test-correlation-789");
            });
    }

    @Test
    void testApplicationUseCaseError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=application_use_case")
            .header("X-Correlation-ID", "test-correlation-app")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("APP_001");
                assert response.getError().getLayer().equals("Application");
                assert response.getError().getMessage().contains("Test use case failed");
                assert response.getCorrelationId().equals("test-correlation-app");
            });
    }

    @Test
    void testInfrastructureDatabaseError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=infrastructure_database")
            .header("X-Correlation-ID", "test-correlation-infra")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("INFRA_001");
                assert response.getError().getLayer().equals("Application");
                assert response.getError().getMessage().contains("Test database error");
                assert response.getCorrelationId().equals("test-correlation-infra");
            });
    }

    @Test
    void testSystemInternalError() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=system_internal")
            .header("X-Correlation-ID", "test-correlation-system")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("SYSTEM_001");
                assert response.getError().getLayer().equals("System");
                assert response.getError().getMessage().contains("An unexpected error occurred");
                assert response.getCorrelationId().equals("test-correlation-system");
            });
    }

    @Test
    void testUnknownErrorType() {
        webTestClient
            .get()
            .uri("/api/v1/test-error?errorType=unknown")
            .header("X-Correlation-ID", "test-correlation-unknown")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody(ErrorResponse.class)
            .value(response -> {
                assert response.getError().getCode().equals("SYSTEM_001");
                assert response.getError().getLayer().equals("System");
                assert response.getCorrelationId().equals("test-correlation-unknown");
            });
    }
}
