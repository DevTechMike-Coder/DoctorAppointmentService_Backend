package com.example.doctorappointmentservice.exception;

/**
 * Thrown when a requested entity (user, doctor, slot, appointment, etc.)
 * cannot be found by id. Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
