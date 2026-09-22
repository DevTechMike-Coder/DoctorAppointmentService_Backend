package com.example.doctorappointmentservice.exception;

/**
 * Thrown by {@code AuthService} when login credentials (email/password)
 * do not match an existing account. Mapped to HTTP 401 by
 * {@link GlobalExceptionHandler}.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
