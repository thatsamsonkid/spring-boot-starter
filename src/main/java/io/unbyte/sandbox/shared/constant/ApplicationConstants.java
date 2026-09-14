package io.unbyte.sandbox.shared.constant;

/**
 * Application-wide constants
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Utility class - prevent instantiation
    }

    // API Constants
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;

    // Date/Time Constants
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    // Validation Constants
    public static final int MAX_STRING_LENGTH = 255;
    public static final int MIN_STRING_LENGTH = 1;
}
