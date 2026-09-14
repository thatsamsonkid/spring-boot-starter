package io.unbyte.sandbox.infrastructure.web.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

/**
 * Configuration for MDC (Mapped Diagnostic Context) bridge in reactive streams
 * This ensures that MDC context is automatically propagated across reactive streams
 * without requiring explicit .doOnNext() calls
 */
@Configuration
public class ReactiveMdcConfig {

    private static final String MDC_CONTEXT_KEY = "mdcContext";

    @PostConstruct
    public void setupMdcContextLifter() {
        // Install the MDC context lifter hook that automatically propagates context
        Hooks.onEachOperator(
                MDC_CONTEXT_KEY,
                Operators.lift(
                        (scannable, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber)));
    }

    @PreDestroy
    public void cleanupMdcContextLifter() {
        // Remove the hook when the application shuts down
        Hooks.resetOnEachOperator(MDC_CONTEXT_KEY);
    }

    /**
     * MDC Context Lifter that automatically propagates MDC context across reactive streams
     * This eliminates the need for explicit .doOnNext() calls to manage context
     */
    private static class MdcContextLifter<T> implements reactor.core.CoreSubscriber<T> {
        private final reactor.core.CoreSubscriber<T> actual;

        MdcContextLifter(reactor.core.CoreSubscriber<T> actual) {
            this.actual = actual;
        }

        @Override
        public void onSubscribe(org.reactivestreams.Subscription subscription) {
            copyContextToMdc();
            try {
                actual.onSubscribe(subscription);
            } finally {
                MDC.clear();
            }
        }

        @Override
        public void onNext(T t) {
            copyContextToMdc();
            try {
                actual.onNext(t);
            } finally {
                MDC.clear();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            copyContextToMdc();
            try {
                actual.onError(throwable);
            } finally {
                MDC.clear();
            }
        }

        @Override
        public void onComplete() {
            copyContextToMdc();
            try {
                actual.onComplete();
            } finally {
                MDC.clear();
            }
        }

        @Override
        public reactor.util.context.Context currentContext() {
            return actual.currentContext();
        }

        /**
         * Copy context from reactive context to MDC
         * This is called automatically for every signal in the reactive stream
         */
        private void copyContextToMdc() {
            Context context = actual.currentContext();
            if (context.hasKey(MDC_CONTEXT_KEY)) {
                @SuppressWarnings("unchecked")
                Map<String, String> mdcContext = context.get(MDC_CONTEXT_KEY);
                if (mdcContext != null) {
                    mdcContext.forEach(MDC::put);
                }
            }
        }
    }
}
