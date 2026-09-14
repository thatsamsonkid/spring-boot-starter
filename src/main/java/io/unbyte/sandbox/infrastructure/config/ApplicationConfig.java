package io.unbyte.sandbox.infrastructure.config;

import io.unbyte.sandbox.application.usecase.GetHealthStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application layer beans.
 *
 * <p>This class handles dependency injection for framework-agnostic use cases. The application
 * layer remains clean of Spring annotations.
 */
@Configuration
public class ApplicationConfig {

    /** Bean for GetHealthStatusUseCase */
    @Bean
    public GetHealthStatusUseCase getHealthStatusUseCase() {
        return new GetHealthStatusUseCase();
    }
}
