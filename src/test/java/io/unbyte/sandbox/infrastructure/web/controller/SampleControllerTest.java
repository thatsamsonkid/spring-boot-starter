package io.unbyte.sandbox.infrastructure.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.unbyte.sandbox.infrastructure.web.request.HelloRequest;
import io.unbyte.sandbox.infrastructure.web.request.RequestItemRecord;
import io.unbyte.sandbox.infrastructure.web.response.HelloResponse;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Integration tests for optional sample APIs. */
@SpringBootTest
@AutoConfigureWebTestClient
class SampleControllerTest {

    @Autowired private WebTestClient webTestClient;

    @Test
    void helloEndpoint_shouldReturnHelloResponse() {
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
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody(HelloResponse.class)
                .value(
                        response -> {
                            assert response.message().contains("Hello from Sandbox Service");
                            assert response.message().contains("3123");
                            assert response.message().contains("4567");
                            assert response.service().equals("sandbox-service");
                            assert response.timestamp() != null;
                        });
    }
}
