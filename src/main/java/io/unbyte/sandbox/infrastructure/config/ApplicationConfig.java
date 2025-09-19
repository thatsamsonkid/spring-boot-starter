package io.unbyte.sandbox.infrastructure.config;

import io.unbyte.sandbox.application.port.ExternalApiPort;
import io.unbyte.sandbox.application.usecase.FetchPostsUseCase;
import io.unbyte.sandbox.application.usecase.GetHealthStatusUseCase;
import io.unbyte.sandbox.application.usecase.ProcessHelloRequestUseCase;
import io.unbyte.sandbox.application.usecase.TestErrorUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application layer beans.
 * 
 * This class handles dependency injection for framework-agnostic use cases.
 * The application layer remains clean of Spring annotations.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Bean for ProcessHelloRequestUseCase
     */
    @Bean
    public ProcessHelloRequestUseCase processHelloRequestUseCase() {
        return new ProcessHelloRequestUseCase();
    }

    /**
     * Bean for GetHealthStatusUseCase
     */
    @Bean
    public GetHealthStatusUseCase getHealthStatusUseCase() {
        return new GetHealthStatusUseCase();
    }

    /**
     * Bean for TestErrorUseCase
     */
    @Bean
    public TestErrorUseCase testErrorUseCase() {
        return new TestErrorUseCase();
    }

    /**
     * Bean for FetchPostsUseCase
     * Requires ExternalApiPort dependency
     */
    @Bean
    public FetchPostsUseCase fetchPostsUseCase(ExternalApiPort externalApiPort) {
        return new FetchPostsUseCase(externalApiPort);
    }
}
