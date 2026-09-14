package io.unbyte.sandbox.infrastructure.web.exception;

import io.unbyte.sandbox.infrastructure.web.response.ErrorResponse;
import io.unbyte.sandbox.shared.exception.ApplicationException;
import io.unbyte.sandbox.shared.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the application
 * Provides centralized error handling and response mapping
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApplicationException(
            ApplicationException ex, ServerWebExchange exchange) {

        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        logger.error(
                "Application exception occurred: {} - Correlation ID: {} - User ID: {}",
                ex.getErrorCode(),
                correlationId,
                userId,
                ex);

        ErrorResponse.ErrorDetails errorDetails =
                new ErrorResponse.ErrorDetails(
                        ex.getErrorCode().getCode(),
                        ex.getMessage(),
                        ex.getLayer(),
                        ex.getOperation(),
                        ex.getContext());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        errorDetails,
                        LocalDateTime.now(),
                        correlationId,
                        exchange.getRequest().getPath().value());

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());

        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {

        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        logger.error(
                "Unexpected exception occurred - Correlation ID: {} - User ID: {}",
                correlationId,
                userId,
                ex);

        ErrorResponse.ErrorDetails errorDetails =
                new ErrorResponse.ErrorDetails(
                        ErrorCode.SYSTEM_INTERNAL_ERROR.getCode(),
                        "An unexpected error occurred",
                        "System",
                        "unknown",
                        Map.of("originalMessage", ex.getMessage()));

        ErrorResponse errorResponse =
                new ErrorResponse(
                        errorDetails,
                        LocalDateTime.now(),
                        correlationId,
                        exchange.getRequest().getPath().value());

        return Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * Map error codes to appropriate HTTP status codes
     */
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case DOMAIN_ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DOMAIN_ENTITY_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case DOMAIN_VALIDATION_FAILED, DOMAIN_CONSTRAINT_VIOLATION -> HttpStatus.BAD_REQUEST;
            case DOMAIN_BUSINESS_RULE_VIOLATION -> HttpStatus.UNPROCESSABLE_ENTITY;
            case WEB_AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
            case WEB_AUTHORIZATION_FAILED -> HttpStatus.FORBIDDEN;
            case WEB_RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case WEB_REQUEST_TIMEOUT -> HttpStatus.REQUEST_TIMEOUT;
            case APPLICATION_TIMEOUT, INFRASTRUCTURE_NETWORK_ERROR -> HttpStatus.GATEWAY_TIMEOUT;
            case INFRASTRUCTURE_EXTERNAL_SERVICE_ERROR -> HttpStatus.BAD_GATEWAY;
            case SYSTEM_OUT_OF_MEMORY, SYSTEM_DISK_FULL -> HttpStatus.INSUFFICIENT_STORAGE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
