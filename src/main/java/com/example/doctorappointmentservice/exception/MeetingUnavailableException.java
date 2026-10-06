package com.example.doctorappointmentservice.exception;

import lombok.Getter;

/**
 * Thrown when a video call can't be joined right now (appointment not confirmed,
 * outside the join window, or the video provider is unavailable). Mapped to HTTP 409
 * (or 503 for provider problems) by {@link GlobalExceptionHandler}.
 */
@Getter
public class MeetingUnavailableException extends RuntimeException {

    private final boolean providerFailure;

    public MeetingUnavailableException(String message) {
        this(message, false);
    }

    public MeetingUnavailableException(String message, boolean providerFailure) {
        super(message);
        this.providerFailure = providerFailure;
    }

}
