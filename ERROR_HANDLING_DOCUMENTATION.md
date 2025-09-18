# Layered Error Handling System

## Overview

This document describes the comprehensive layered error handling system implemented in the Sandbox application. The system provides clear identification of where and why failures occur across all architectural layers.

## Architecture

### Error Hierarchy

```
ApplicationException (Base)
├── DomainException
├── ApplicationServiceException
├── InfrastructureException
└── WebException (via GlobalExceptionHandler)
```

### Error Code Structure

Each error code follows the pattern: `{LAYER}_{TYPE}_{NUMBER}`

- **Domain Layer**: `DOMAIN_001` - `DOMAIN_006`
- **Application Layer**: `APP_001` - `APP_006`
- **Infrastructure Layer**: `INFRA_001` - `INFRA_006`
- **Web Layer**: `WEB_001` - `WEB_006`
- **Shared Layer**: `SHARED_001` - `SHARED_005`
- **System Layer**: `SYSTEM_001` - `SYSTEM_005`

## Components

### 1. ErrorCode Enum

Centralized error codes with layer identification.

```java
public enum ErrorCode {
    DOMAIN_VALIDATION_FAILED("DOMAIN_001", "Domain validation failed", "Domain"),
    DOMAIN_ENTITY_NOT_FOUND("DOMAIN_003", "Entity not found", "Domain"),
    // ... more error codes
}
```

### 2. ApplicationException

Base exception with structured context:

```java
public class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String correlationId;
    private final String userId;
    private final String layer;
    private final String operation;
    private final Map<String, Object> context;
}
```

### 3. Layer-Specific Exceptions

#### Domain Layer

```java
// Domain validation error
throw DomainException.validationFailed("email", "Invalid email format");

// Entity not found
throw DomainException.entityNotFound("User", "user-123");

// Business rule violation
throw DomainException.businessRuleViolation("AgeRule", "User must be 18+");
```

#### Application Layer

```java
// Use case failure
throw ApplicationServiceException.useCaseFailed("createUser", "Validation failed");

// Command validation
throw ApplicationServiceException.commandValidationFailed("CreateUserCommand", "Missing required fields");
```

#### Infrastructure Layer

```java
// Database error
throw InfrastructureException.databaseError("save", databaseException);

// External service error
throw InfrastructureException.externalServiceError("PaymentService", "processPayment", serviceException);
```

### 4. Global Exception Handler

Automatically converts exceptions to standardized error responses:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApplicationException(
            ApplicationException ex, ServerWebExchange exchange) {
        // Convert to structured error response
    }
}
```

### 5. Error Response Format

```json
{
  "error": {
    "code": "DOMAIN_001",
    "message": "Domain validation failed",
    "layer": "Domain",
    "operation": "createUser",
    "context": {
      "field": "email",
      "value": "invalid-email"
    }
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "correlationId": "corr-12345678",
  "path": "/api/v1/users"
}
```

### 6. Error Tracking Service

Integrates with metrics and logging:

```java
@Service
public class ErrorTrackingService {

    public void trackError(ApplicationException exception) {
        // Log structured error information
        // Record metrics
        // Track error details
    }
}
```

## HTTP Status Code Mapping

| Error Code                 | HTTP Status | Description                  |
| -------------------------- | ----------- | ---------------------------- |
| `DOMAIN_003`               | 404         | Entity not found             |
| `DOMAIN_004`               | 409         | Entity already exists        |
| `DOMAIN_001`, `DOMAIN_006` | 400         | Validation/constraint errors |
| `DOMAIN_002`               | 422         | Business rule violation      |
| `WEB_002`                  | 401         | Authentication failed        |
| `WEB_003`                  | 403         | Authorization failed         |
| `WEB_006`                  | 429         | Rate limit exceeded          |
| `WEB_004`                  | 408         | Request timeout              |
| `INFRA_003`                | 504         | Network timeout              |
| `INFRA_002`                | 502         | External service error       |
| `SYSTEM_002`, `SYSTEM_003` | 507         | System resource issues       |
| Default                    | 500         | Internal server error        |

## Usage Examples

### Domain Service

```java
@Service
public class UserService {
    public User createUser(String email) {
        if (email == null || !email.contains("@")) {
            throw DomainException.validationFailed("email", "Invalid email format");
        }

        if (userExists(email)) {
            throw DomainException.entityAlreadyExists("User", email);
        }

        // Business logic...
    }
}
```

### Application Service

```java
@Service
public class UserApplicationService {
    public UserDto createUser(CreateUserCommand command) {
        try {
            return userService.createUser(command.getEmail());
        } catch (DomainException ex) {
            throw ApplicationServiceException.useCaseFailed("createUser", ex.getMessage())
                .correlationId(MDC.get("correlationId"))
                .userId(MDC.get("userId"))
                .layer("Application")
                .operation("createUser")
                .build();
        }
    }
}
```

### Infrastructure Service

```java
@Repository
public class DatabaseUserRepository {
    public User save(User user) {
        try {
            return jpaRepository.save(user);
        } catch (DataAccessException ex) {
            throw InfrastructureException.databaseError("save", ex)
                .correlationId(MDC.get("correlationId"))
                .layer("Infrastructure")
                .operation("save")
                .build();
        }
    }
}
```

## Testing Error Handling

### Test Endpoint

```bash
# Test domain validation error
curl -X GET "http://localhost:8080/api/v1/test-error?errorType=domain_validation" \
  -H "X-Correlation-ID: test-123"

# Test domain entity not found
curl -X GET "http://localhost:8080/api/v1/test-error?errorType=domain_entity_not_found" \
  -H "X-Correlation-ID: test-456"

# Test application use case error
curl -X GET "http://localhost:8080/api/v1/test-error?errorType=application_use_case" \
  -H "X-Correlation-ID: test-789"
```

### Expected Responses

#### Domain Validation Error (400)

```json
{
  "error": {
    "code": "DOMAIN_001",
    "message": "Validation failed for field testField: Test validation error",
    "layer": "Domain",
    "operation": null,
    "context": null
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "correlationId": "test-123",
  "path": "/api/v1/test-error"
}
```

#### Domain Entity Not Found (404)

```json
{
  "error": {
    "code": "DOMAIN_003",
    "message": "Entity TestEntity with ID test-id-123 not found",
    "layer": "Domain",
    "operation": null,
    "context": null
  },
  "timestamp": "2024-01-01T12:00:00.000Z",
  "correlationId": "test-456",
  "path": "/api/v1/test-error"
}
```

## Benefits

1. **Clear Error Identification**: Know exactly which layer and operation failed
2. **Structured Error Information**: Consistent error format across all layers
3. **Context Preservation**: Correlation IDs and user context maintained
4. **Metrics Integration**: Automatic error tracking and classification
5. **HTTP Status Mapping**: Appropriate HTTP status codes for each error type
6. **Logging Integration**: Structured logging with error context
7. **Debugging Support**: Rich context information for troubleshooting

## Monitoring and Metrics

The error handling system integrates with Micrometer for metrics collection:

- **Error Counters**: Track errors by code, layer, and type
- **Error Timers**: Measure error response times
- **Error Gauges**: Monitor error rates
- **Custom Tags**: Layer, operation, and error type tags

## Best Practices

1. **Use Specific Error Codes**: Choose the most specific error code for the situation
2. **Provide Context**: Include relevant context information in exceptions
3. **Preserve Correlation IDs**: Always include correlation IDs for tracing
4. **Log Appropriately**: Use appropriate log levels for different error types
5. **Handle Gracefully**: Convert lower-level exceptions to higher-level ones with context
6. **Test Error Paths**: Include error scenarios in your tests
7. **Monitor Error Rates**: Set up alerts for high error rates

## Integration with Existing Systems

The error handling system integrates seamlessly with:

- **Spring Boot Actuator**: Health checks and metrics
- **Micrometer**: Metrics collection and monitoring
- **SLF4J**: Structured logging
- **Reactive Context**: Context propagation in reactive streams
- **ArchUnit**: Architecture enforcement
- **Prometheus**: Metrics export for monitoring dashboards

