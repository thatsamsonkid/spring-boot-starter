package io.unbyte.sandbox.infrastructure.web.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.UUID;

/**
 * Web filter to capture and propagate headers and context across reactive streams
 * This ensures that headers and context are maintained even when threads switch
 */
@Component
public class ContextFilter implements WebFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    
    // Headers to propagate
    private static final String[] HEADERS_TO_PROPAGATE = {
        "X-User-ID",
        "X-Trace-ID", 
        "X-Request-ID",
        "X-Correlation-ID",
        "Authorization",
        "X-Forwarded-For",
        "X-Real-IP"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract headers and create context
        Map<String, String> contextMap = extractContextFromHeaders(request);
        
        // Generate correlation ID if not present
        String correlationId = contextMap.getOrDefault(CORRELATION_ID_MDC_KEY, 
            generateCorrelationId());
        contextMap.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Create reactive context with all the extracted values
        Context reactiveContext = Context.empty();
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            reactiveContext = reactiveContext.put(entry.getKey(), entry.getValue());
        }
        
        return chain.filter(exchange)
            .contextWrite(reactiveContext)
            .contextWrite(Context.of("mdcContext", contextMap))
            .doOnEach(signal -> {
                // Set MDC for each signal in the reactive stream
                if (signal.hasValue() || signal.hasError()) {
                    contextMap.forEach(MDC::put);
                }
            })
            .doFinally(signalType -> {
                // Clear MDC after processing
                MDC.clear();
            });
    }

    /**
     * Extract context information from request headers
     */
    private Map<String, String> extractContextFromHeaders(ServerHttpRequest request) {
        Map<String, String> contextMap = new java.util.HashMap<>();
        
        // Extract specific headers
        request.getHeaders().forEach((headerName, headerValues) -> {
            if (headerValues != null && !headerValues.isEmpty()) {
                String headerValue = headerValues.get(0);
                
                switch (headerName.toLowerCase()) {
                    case "x-user-id":
                        contextMap.put(USER_ID_MDC_KEY, headerValue);
                        break;
                    case "x-trace-id":
                        contextMap.put(TRACE_ID_MDC_KEY, headerValue);
                        break;
                    case "x-request-id":
                        contextMap.put(REQUEST_ID_MDC_KEY, headerValue);
                        break;
                    case "x-correlation-id":
                        contextMap.put(CORRELATION_ID_MDC_KEY, headerValue);
                        break;
                }
            }
        });
        
        // Generate request ID if not present
        if (!contextMap.containsKey(REQUEST_ID_MDC_KEY)) {
            contextMap.put(REQUEST_ID_MDC_KEY, generateRequestId());
        }
        
        return contextMap;
    }

    /**
     * Generate a unique correlation ID
     */
    private String generateCorrelationId() {
        return "corr-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate a unique request ID
     */
    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public int getOrder() {
        // High priority to ensure this filter runs early
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
