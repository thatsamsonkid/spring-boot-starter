package io.unbyte.sandbox.infrastructure.exception;

import io.unbyte.sandbox.shared.exception.ApplicationException;
import io.unbyte.sandbox.shared.exception.ErrorCode;

/**
 * Infrastructure layer exceptions
 */
public class InfrastructureException extends ApplicationException {
    
    public InfrastructureException(ErrorCode errorCode, String message) {
        super(errorCode, message, null, null, null, "Infrastructure", null, null);
    }

    public InfrastructureException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause, null, null, "Infrastructure", null, null);
    }

    public static InfrastructureException databaseError(String operation, Throwable cause) {
        return new InfrastructureException(
            ErrorCode.INFRASTRUCTURE_DATABASE_ERROR,
            String.format("Database operation %s failed", operation),
            cause
        );
    }

    public static InfrastructureException externalServiceError(String service, String operation, Throwable cause) {
        return new InfrastructureException(
            ErrorCode.INFRASTRUCTURE_EXTERNAL_SERVICE_ERROR,
            String.format("External service %s operation %s failed", service, operation),
            cause
        );
    }
}

