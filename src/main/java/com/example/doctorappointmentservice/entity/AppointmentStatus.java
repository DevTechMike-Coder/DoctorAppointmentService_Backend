package com.example.doctorappointmentservice.entity;

/**
 * Lifecycle states of an {@link Appointment}.
 */
public enum AppointmentStatus {
    /** Booked by the patient but not yet confirmed by the doctor. */
    PENDING,
    /** Confirmed by the doctor/admin and expected to take place. */
    CONFIRMED,
    /** Cancelled by either party; the underlying slot is freed up. */
    CANCELLED,
    /** The appointment has taken place. */
    COMPLETED
}
