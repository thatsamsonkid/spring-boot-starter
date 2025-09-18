package io.unbyte.sandbox.application.exception;

import io.unbyte.sandbox.shared.exception.ApplicationException;
import io.unbyte.sandbox.shared.exception.ErrorCode;

/**
 * Application layer exceptions
 */
public class ApplicationServiceException extends ApplicationException {
    
    public ApplicationServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message, null, null, null, "Application", null, null);
    }

    public ApplicationServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause, null, null, "Application", null, null);
    }

    public static ApplicationServiceException useCaseFailed(String useCase, String reason) {
        return new ApplicationServiceException(
            ErrorCode.APPLICATION_USE_CASE_FAILED,
            String.format("Use case %s failed: %s", useCase, reason)
        );
    }

    public static ApplicationServiceException commandValidationFailed(String command, String details) {
        return new ApplicationServiceException(
            ErrorCode.APPLICATION_COMMAND_VALIDATION_FAILED,
            String.format("Command validation failed for %s: %s", command, details)
        );
    }
}

