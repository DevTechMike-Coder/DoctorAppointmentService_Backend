package com.example.doctorappointmentservice.exception;

/**
 * Thrown when a patient attempts to book an {@code AvailabilitySlot} that
 * has already been booked. Mapped to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
