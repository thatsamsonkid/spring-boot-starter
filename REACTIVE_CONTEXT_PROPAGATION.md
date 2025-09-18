# Reactive Context Propagation

This document explains how context and headers are maintained across reactive streams in the Sandbox application.

## Problem

In reactive programming with Spring WebFlux, headers and context are not automatically maintained between threads because of the asynchronous nature of reactive streams. When a request switches threads during processing, the original context (like correlation IDs, user IDs, etc.) can be lost.

## Solution

We've implemented a comprehensive solution using:

1. **ContextFilter** - Web filter to capture and propagate headers
2. **ReactiveMdcConfig** - MDC bridge for reactive streams
3. **ReactiveContextUtil** - Utility class for context management

## Components

### 1. ContextFilter

A web filter that:

- Captures headers from incoming requests
- Generates correlation IDs if missing
- Propagates context through reactive streams
- Sets MDC for logging

**Key Features:**

- Captures headers: `X-Correlation-ID`, `X-User-ID`, `X-Trace-ID`, `X-Request-ID`
- Generates correlation ID if not present
- Adds correlation ID to response headers
- High priority filter (runs early)

### 2. ReactiveMdcConfig

Configuration that:

- Installs MDC context lifter hook
- Ensures MDC context is propagated across reactive streams
- Automatically cleans up context after processing

**How it works:**

- Uses Reactor's `Hooks.onEachOperator` to install a context lifter
- The lifter copies context to MDC for each signal in the reactive stream
- Clears MDC after processing to prevent memory leaks

### 3. ReactiveContextUtil

Utility class providing:

- Methods to get/set context values in reactive streams
- Context propagation helpers
- MDC integration utilities

**Key Methods:**

- `getCorrelationId()` - Get correlation ID from context
- `getUserId()` - Get user ID from context
- `withContext()` - Set context values
- `withMdcContext()` - Propagate context to MDC

## Usage Examples

### In Controllers

```java
@PostMapping("/hello")
public Mono<HelloResponse> hello(@RequestBody HelloRequest request) {
    return Mono.fromCallable(() -> {
        // Context is automatically available in MDC due to the MDC bridge
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        logger.info("Processing with correlation ID: {}", correlationId);
        logger.info("Processing for user: {}", userId);

        return processRequest(request);
    });
}
```

### Context Propagation

```java
// Get context values
Mono<String> correlationId = ReactiveContextUtil.getCorrelationId();
Mono<String> userId = ReactiveContextUtil.getUserId();

// Set context values
Mono<String> result = ReactiveContextUtil.withContext("customKey", "customValue",
    Mono.just("processing"));
```

## Headers Supported

| Header             | Context Key     | Description            |
| ------------------ | --------------- | ---------------------- |
| `X-Correlation-ID` | `correlationId` | Request correlation ID |
| `X-User-ID`        | `userId`        | User identifier        |
| `X-Trace-ID`       | `traceId`       | Distributed tracing ID |
| `X-Request-ID`     | `requestId`     | Request identifier     |
| `Authorization`    | -               | Authentication token   |
| `X-Forwarded-For`  | -               | Client IP address      |
| `X-Real-IP`        | -               | Real client IP         |

## Testing

The solution includes comprehensive tests:

```java
@Test
void helloEndpoint_shouldPropagateContext() {
    webTestClient
        .post()
        .uri("/api/v1/hello")
        .header("X-Correlation-ID", "test-correlation-123")
        .header("X-User-ID", "user-456")
        .contentType(APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().exists("X-Correlation-ID");
}
```

## Benefits

1. **Context Preservation**: Headers and context are maintained across thread switches
2. **Automatic Propagation**: No manual context passing required - no explicit `.doOnNext()` calls needed
3. **Clean Code**: Direct access to context via `MDC.get()` - no complex reactive chains
4. **Logging Integration**: MDC context is automatically available for logging
5. **Correlation Tracking**: Requests can be tracked through the system
6. **User Context**: User information is preserved throughout the request lifecycle

## Configuration

The solution is automatically configured when the application starts:

1. **ContextFilter** is registered as a Spring component
2. **ReactiveMdcConfig** installs the MDC bridge
3. **ReactiveContextUtil** provides utility methods

No additional configuration is required.

## Best Practices

1. **Use direct MDC access** - `MDC.get("correlationId")` instead of complex reactive chains
2. **Log correlation IDs** in important operations
3. **Propagate context** when calling external services
4. **Use structured logging** with context values
5. **Test context propagation** in integration tests
6. **Keep controllers simple** - no need for explicit context management

## Example Logs

With context propagation, logs will include correlation information:

```
2024-01-01T12:00:00.000Z [correlationId=corr-12345678] [userId=user-456] INFO - Processing hello request
2024-01-01T12:00:00.001Z [correlationId=corr-12345678] [userId=user-456] INFO - Hello response created successfully
```

This makes it easy to trace requests through the system and debug issues.
