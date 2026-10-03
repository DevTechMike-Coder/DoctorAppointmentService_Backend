package com.example.doctorappointmentservice.exception;

/** Thrown when valid credentials belong to an account whose email hasn't been verified yet. */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
