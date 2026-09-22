package com.example.doctorappointmentservice.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard JSON error body returned to API clients by
 * {@link GlobalExceptionHandler} whenever a request fails.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    /**
     * Convenience constructor for simple errors with no field-level details.
     * Timestamps the response with the current time.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    /**
     * Convenience constructor for errors that include a list of field-level
     * validation messages. Timestamps the response with the current time.
     */
    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}