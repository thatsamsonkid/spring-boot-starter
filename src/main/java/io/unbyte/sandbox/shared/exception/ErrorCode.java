package io.unbyte.sandbox.shared.exception;

/**
 * Centralized error codes for the Sandbox application
 * Provides clear identification of where and why failures occur
 */
public enum ErrorCode {

    // ===== DOMAIN LAYER ERRORS =====
    DOMAIN_VALIDATION_FAILED("DOMAIN_001", "Domain validation failed", "Domain"),
    DOMAIN_BUSINESS_RULE_VIOLATION("DOMAIN_002", "Business rule violation", "Domain"),
    DOMAIN_ENTITY_NOT_FOUND("DOMAIN_003", "Entity not found", "Domain"),
    DOMAIN_ENTITY_ALREADY_EXISTS("DOMAIN_004", "Entity already exists", "Domain"),
    DOMAIN_INVALID_STATE_TRANSITION("DOMAIN_005", "Invalid state transition", "Domain"),
    DOMAIN_CONSTRAINT_VIOLATION("DOMAIN_006", "Domain constraint violation", "Domain"),

    // ===== APPLICATION LAYER ERRORS =====
    APPLICATION_USE_CASE_FAILED("APP_001", "Use case execution failed", "Application"),
    APPLICATION_COMMAND_VALIDATION_FAILED("APP_002", "Command validation failed", "Application"),
    APPLICATION_QUERY_FAILED("APP_003", "Query execution failed", "Application"),
    APPLICATION_BUSINESS_LOGIC_ERROR("APP_004", "Business logic error", "Application"),
    APPLICATION_SERVICE_UNAVAILABLE("APP_005", "Application service unavailable", "Application"),
    APPLICATION_TIMEOUT("APP_006", "Application operation timeout", "Application"),

    // ===== INFRASTRUCTURE LAYER ERRORS =====
    INFRASTRUCTURE_DATABASE_ERROR("INFRA_001", "Database operation failed", "Infrastructure"),
    INFRASTRUCTURE_EXTERNAL_SERVICE_ERROR("INFRA_002", "External service error", "Infrastructure"),
    INFRASTRUCTURE_NETWORK_ERROR("INFRA_003", "Network communication error", "Infrastructure"),
    INFRASTRUCTURE_CONFIGURATION_ERROR("INFRA_004", "Configuration error", "Infrastructure"),
    INFRASTRUCTURE_PERSISTENCE_ERROR("INFRA_005", "Data persistence error", "Infrastructure"),
    INFRASTRUCTURE_MESSAGING_ERROR("INFRA_006", "Messaging system error", "Infrastructure"),

    // ===== WEB LAYER ERRORS =====
    WEB_REQUEST_VALIDATION_FAILED("WEB_001", "Request validation failed", "Web"),
    WEB_AUTHENTICATION_FAILED("WEB_002", "Authentication failed", "Web"),
    WEB_AUTHORIZATION_FAILED("WEB_003", "Authorization failed", "Web"),
    WEB_REQUEST_TIMEOUT("WEB_004", "Request timeout", "Web"),
    WEB_MALFORMED_REQUEST("WEB_005", "Malformed request", "Web"),
    WEB_RATE_LIMIT_EXCEEDED("WEB_006", "Rate limit exceeded", "Web"),

    // ===== SHARED LAYER ERRORS =====
    SHARED_SERIALIZATION_ERROR("SHARED_001", "Serialization error", "Shared"),
    SHARED_DESERIALIZATION_ERROR("SHARED_002", "Deserialization error", "Shared"),
    SHARED_VALIDATION_ERROR("SHARED_003", "Validation error", "Shared"),
    SHARED_UTILITY_ERROR("SHARED_004", "Utility function error", "Shared"),
    SHARED_CONSTANT_ERROR("SHARED_005", "Constant definition error", "Shared"),

    // ===== SYSTEM ERRORS =====
    SYSTEM_INTERNAL_ERROR("SYSTEM_001", "Internal system error", "System"),
    SYSTEM_OUT_OF_MEMORY("SYSTEM_002", "Out of memory", "System"),
    SYSTEM_DISK_FULL("SYSTEM_003", "Disk space full", "System"),
    SYSTEM_THREAD_POOL_EXHAUSTED("SYSTEM_004", "Thread pool exhausted", "System"),
    SYSTEM_GC_OVERHEAD("SYSTEM_005", "GC overhead limit exceeded", "System");

    private final String code;
    private final String message;
    private final String layer;

    ErrorCode(String code, String message, String layer) {
        this.code = code;
        this.message = message;
        this.layer = layer;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getLayer() {
        return layer;
    }

    /**
     * Get error code by code string
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }

    /**
     * Get all error codes for a specific layer
     */
    public static ErrorCode[] getByLayer(String layer) {
        return java.util.Arrays.stream(values())
                .filter(errorCode -> errorCode.layer.equals(layer))
                .toArray(ErrorCode[]::new);
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", code, message, layer);
    }
}
