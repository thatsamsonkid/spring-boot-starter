package io.unbyte.sandbox.infrastructure.config;

import io.unbyte.sandbox.application.port.ExternalApiPort;
import io.unbyte.sandbox.application.usecase.FetchPostsUseCase;
import io.unbyte.sandbox.application.usecase.ProcessHelloRequestUseCase;
import io.unbyte.sandbox.application.usecase.TestErrorUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans for optional sample use cases. Removed when a project is generated without sample code.
 */
@Configuration
public class SampleApplicationConfig {

    /** Bean for ProcessHelloRequestUseCase */
    @Bean
    public ProcessHelloRequestUseCase processHelloRequestUseCase() {
        return new ProcessHelloRequestUseCase();
    }

    /** Bean for TestErrorUseCase */
    @Bean
    public TestErrorUseCase testErrorUseCase() {
        return new TestErrorUseCase();
    }

    /** Bean for FetchPostsUseCase. Requires ExternalApiPort dependency. */
    @Bean
    public FetchPostsUseCase fetchPostsUseCase(ExternalApiPort externalApiPort) {
        return new FetchPostsUseCase(externalApiPort);
    }
}
