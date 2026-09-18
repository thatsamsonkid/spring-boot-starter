package io.unbyte.sandbox.infrastructure.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata used by springdoc when generating /v3/api-docs and Swagger UI.
 *
 * <p>Kept in the web layer so domain and application stay framework-agnostic.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sandboxOpenAPI(
            @Value("${spring.application.name}") String applicationName,
            @Value("${springdoc.info.description:Sandbox REST API}") String description,
            @Value("${springdoc.info.version:1.0.0}") String version) {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(applicationName + " API")
                                .description(description)
                                .version(version));
    }
}
