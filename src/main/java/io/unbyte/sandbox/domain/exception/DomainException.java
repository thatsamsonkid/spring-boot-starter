package io.unbyte.sandbox.domain.exception;

import io.unbyte.sandbox.shared.exception.ApplicationException;
import io.unbyte.sandbox.shared.exception.ErrorCode;

/**
 * Domain-specific exceptions
 */
public class DomainException extends ApplicationException {
    
    public DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message, null, null, null, "Domain", null, null);
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause, null, null, "Domain", null, null);
    }

    public static DomainException entityNotFound(String entityType, String entityId) {
        return new DomainException(
            ErrorCode.DOMAIN_ENTITY_NOT_FOUND,
            String.format("Entity %s with ID %s not found", entityType, entityId)
        );
    }

    public static DomainException businessRuleViolation(String rule, String details) {
        return new DomainException(
            ErrorCode.DOMAIN_BUSINESS_RULE_VIOLATION,
            String.format("Business rule violation: %s - %s", rule, details)
        );
    }

    public static DomainException validationFailed(String field, String reason) {
        return new DomainException(
            ErrorCode.DOMAIN_VALIDATION_FAILED,
            String.format("Validation failed for field %s: %s", field, reason)
        );
    }
}
