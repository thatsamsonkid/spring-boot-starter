# Service Performance Monitoring & Error Tracking

This document describes the comprehensive monitoring solution implemented for the Sandbox service, including performance metrics collection and error tracking with correlation IDs.

## Overview

The monitoring solution provides:

- **Performance Metrics**: Request duration, operation timing, throughput
- **Error Tracking**: Exception classification, error rates by endpoint
- **Context Propagation**: Correlation IDs, user tracking, distributed tracing
- **Real-time Monitoring**: Active request counts, system health

## Architecture

### Components

1. **MetricsConfig** - Centralized metrics configuration
2. **MonitoringFilter** - Request/response monitoring
3. **PerformanceMonitoringService** - Operation-level monitoring
4. **ContextFilter** - Context propagation (correlation IDs, user tracking)

### Metrics Collected

#### Request Metrics

- `sandbox.requests.total` - Total requests processed
- `sandbox.requests.success` - Successful requests
- `sandbox.requests.error` - Failed requests
- `sandbox.requests.duration` - Request processing time
- `sandbox.requests.active` - Currently active requests

#### Operation Metrics

- `sandbox.operations.duration` - Operation execution time
- `sandbox.operations.success` - Successful operations
- `sandbox.operations.error` - Failed operations

#### Error Metrics

- `sandbox.errors.by.type` - Errors by exception type
- `sandbox.errors.by.endpoint` - Errors by endpoint

## Configuration

### Dependencies

```xml
<!-- Micrometer for metrics collection -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
```

### Application Properties

```yaml
# Actuator configuration for metrics
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: sandbox-service
      environment: development
    distribution:
      percentiles-histogram:
        http.server.requests: true
        sandbox.requests.duration: true
        sandbox.operations.duration: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        sandbox.requests.duration: 0.5, 0.95, 0.99
        sandbox.operations.duration: 0.5, 0.95, 0.99
```

## Usage

### Automatic Monitoring

The monitoring is automatically applied to all endpoints:

```java
@PostMapping("/hello")
public Mono<HelloResponse> hello(@RequestBody HelloRequest request) {
    return performanceMonitoringService.monitorOperation("hello_endpoint",
        Mono.fromCallable(() -> {
            // Your business logic here
            return processRequest(request);
        })
    );
}
```

### Manual Operation Monitoring

```java
// Monitor a reactive operation
Mono<String> result = performanceMonitoringService.monitorOperation(
    "custom_operation",
    yourMonoOperation
);

// Monitor a function
String result = performanceMonitoringService.monitorFunction(
    "custom_function",
    input,
    this::processInput
);
```

### Custom Tags

```java
// Create custom tags for operations
Tag[] customTags = performanceMonitoringService.createTags(
    "environment", "production",
    "version", "1.0.0",
    "region", "us-east-1"
);

Mono<String> result = performanceMonitoringService.monitorOperation(
    "tagged_operation",
    yourMonoOperation,
    customTags
);
```

## Monitoring Endpoints

### Actuator Endpoints

- **Health**: `GET /actuator/health` - Service health status
- **Metrics**: `GET /actuator/metrics` - Available metrics
- **Prometheus**: `GET /actuator/prometheus` - Prometheus format metrics

### Key Metrics Endpoints

```bash
# Get all metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric
curl http://localhost:8080/actuator/metrics/sandbox.requests.total

# Get Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## Logging Integration

### Structured Logging

All logs include correlation context:

```
2024-01-01T12:00:00.000Z [correlationId=corr-12345678] [userId=user-456] INFO - Processing hello request with correlation ID: corr-12345678
2024-01-01T12:00:00.001Z [correlationId=corr-12345678] [userId=user-456] INFO - Operation completed successfully: hello_endpoint in 15ms
```

### Log Pattern

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] [%X{userId}] %logger{36} - %msg%n"
```

## Error Tracking

### Automatic Error Classification

Errors are automatically tagged with:

- **Exception Type**: `IllegalArgumentException`, `NullPointerException`, etc.
- **Endpoint**: `hello`, `health`, etc.
- **Correlation ID**: For request tracing
- **User ID**: When available

### Error Metrics

```bash
# Errors by type
curl http://localhost:8080/actuator/metrics/sandbox.errors.by.type

# Errors by endpoint
curl http://localhost:8080/actuator/metrics/sandbox.errors.by.endpoint

# Error rate
curl http://localhost:8080/actuator/metrics/sandbox.requests.error
```

## Performance Monitoring

### Request Timing

- **Total Duration**: End-to-end request processing time
- **Operation Duration**: Individual operation execution time
- **Percentiles**: P50, P95, P99 response times

### Active Request Tracking

```bash
# Current active requests
curl http://localhost:8080/actuator/metrics/sandbox.requests.active
```

## Prometheus Integration

### Metrics Format

```prometheus
# HELP sandbox_requests_total Total number of requests processed
# TYPE sandbox_requests_total counter
sandbox_requests_total{endpoint="hello",method="POST",service="sandbox"} 42.0

# HELP sandbox_requests_duration_seconds Request processing duration
# TYPE sandbox_requests_duration_seconds histogram
sandbox_requests_duration_seconds_bucket{endpoint="hello",method="POST",service="sandbox",le="0.005"} 10.0
sandbox_requests_duration_seconds_bucket{endpoint="hello",method="POST",service="sandbox",le="0.01"} 25.0
sandbox_requests_duration_seconds_bucket{endpoint="hello",method="POST",service="sandbox",le="+Inf"} 42.0
```

### Grafana Dashboards

Recommended dashboard panels:

- Request rate (requests/second)
- Response time percentiles (P50, P95, P99)
- Error rate by endpoint
- Active request count
- Error breakdown by type

## Testing

### Metrics Verification

```java
@Test
void helloEndpoint_shouldRecordMetrics() {
    // Make request
    webTestClient
        .post()
        .uri("/api/v1/hello")
        .header("X-Correlation-ID", "test-correlation-123")
        .contentType(APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk();

    // Verify metrics were recorded
    Counter requestCounter = meterRegistry.find("sandbox.requests.total").counter();
    assertThat(requestCounter.count()).isGreaterThan(0);
}
```

## Best Practices

### 1. Correlation ID Propagation

- Always include `X-Correlation-ID` header in requests
- Use correlation IDs for distributed tracing
- Log correlation IDs in all operations

### 2. Error Classification

- Use specific exception types for better error tracking
- Include context in error messages
- Tag errors with relevant metadata

### 3. Performance Monitoring

- Monitor both request-level and operation-level metrics
- Use percentiles for SLA monitoring
- Track active requests for capacity planning

### 4. Alerting

- Set up alerts for high error rates
- Monitor response time percentiles
- Track active request thresholds

## Troubleshooting

### Common Issues

1. **Metrics Not Appearing**

   - Check actuator endpoints are enabled
   - Verify Micrometer dependencies
   - Ensure proper configuration

2. **Context Not Propagating**

   - Verify ContextFilter is registered
   - Check MDC bridge configuration
   - Ensure headers are being passed

3. **Performance Impact**
   - Monitor metrics collection overhead
   - Use sampling for high-volume endpoints
   - Consider async metric recording

### Debug Commands

```bash
# Check all available metrics
curl http://localhost:8080/actuator/metrics | jq

# Check specific metric details
curl http://localhost:8080/actuator/metrics/sandbox.requests.total | jq

# Verify Prometheus format
curl http://localhost:8080/actuator/prometheus | grep sandbox
```

## Production Considerations

### Scaling

- Use distributed tracing for microservices
- Implement metric aggregation
- Consider metric sampling for high volume

### Security

- Secure actuator endpoints in production
- Use authentication for metrics access
- Filter sensitive data from logs

### Performance

- Monitor metrics collection overhead
- Use async metric recording where possible
- Consider metric retention policies

This monitoring solution provides comprehensive observability for the Sandbox service, enabling effective performance monitoring, error tracking, and distributed tracing across the application.
