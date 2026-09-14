package io.unbyte.sandbox.shared.util;

import java.util.Objects;

/**
 * Utility class for common validation operations
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that an object is not null
     * @param object the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if object is null
     */
    public static void requireNonNull(Object object, String fieldName) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a string is not null or empty
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if string is null or empty
     */
    public static void requireNonEmpty(String value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
