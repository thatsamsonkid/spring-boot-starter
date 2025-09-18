package io.unbyte.sandbox.infrastructure.web.util;

import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing context in reactive streams
 * Provides methods to get, set, and propagate context values
 */
public class ReactiveContextUtil {

    private static final String MDC_CONTEXT_KEY = "mdcContext";
    
    // Common context keys
    public static final String CORRELATION_ID = "correlationId";
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String TRACE_ID = "traceId";

    /**
     * Get a context value from the current reactive context
     */
    public static <T> Mono<T> getContextValue(String key, Class<T> type) {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(key)) {
                return Mono.just(ctx.get(key));
            }
            return Mono.empty();
        });
    }

    /**
     * Get a context value with a default value
     */
    public static <T> Mono<T> getContextValue(String key, T defaultValue, Class<T> type) {
        return getContextValue(key, type)
            .switchIfEmpty(Mono.just(defaultValue));
    }

    /**
     * Set a context value in the reactive context
     */
    public static <T> Mono<T> withContext(String key, Object value, Mono<T> mono) {
        return mono.contextWrite(Context.of(key, value));
    }

    /**
     * Set multiple context values
     */
    public static <T> Mono<T> withContext(Map<String, Object> contextMap, Mono<T> mono) {
        Context context = Context.empty();
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            context = context.put(entry.getKey(), entry.getValue());
        }
        return mono.contextWrite(context);
    }

    /**
     * Get correlation ID from context
     */
    public static Mono<String> getCorrelationId() {
        return getContextValue(CORRELATION_ID, String.class);
    }

    /**
     * Get request ID from context
     */
    public static Mono<String> getRequestId() {
        return getContextValue(REQUEST_ID, String.class);
    }

    /**
     * Get user ID from context
     */
    public static Mono<String> getUserId() {
        return getContextValue(USER_ID, String.class);
    }

    /**
     * Get trace ID from context
     */
    public static Mono<String> getTraceId() {
        return getContextValue(TRACE_ID, String.class);
    }

    /**
     * Create a context map from current MDC
     */
    public static Map<String, Object> createContextFromMdc() {
        Map<String, Object> contextMap = new HashMap<>();
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        if (mdcMap != null) {
            contextMap.putAll(mdcMap);
        }
        return contextMap;
    }

    /**
     * Propagate context to MDC for the duration of the operation
     */
    public static <T> Mono<T> withMdcContext(Mono<T> mono) {
        return mono.doOnEach(signal -> {
            if (signal.hasValue() || signal.hasError()) {
                // Set MDC from reactive context
                reactor.util.context.ContextView context = signal.getContextView();
                if (context.hasKey(MDC_CONTEXT_KEY)) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> mdcContext = context.get(MDC_CONTEXT_KEY);
                    if (mdcContext != null) {
                        mdcContext.forEach(MDC::put);
                    }
                }
            }
        }).doFinally(signalType -> {
            // Clear MDC after processing
            MDC.clear();
        });
    }

    /**
     * Create a reactive context with MDC values
     */
    public static Context createReactiveContext() {
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        if (mdcMap == null || mdcMap.isEmpty()) {
            return Context.empty();
        }
        return Context.of(MDC_CONTEXT_KEY, mdcMap);
    }
}
