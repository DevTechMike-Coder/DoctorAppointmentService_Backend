package com.example.doctorappointmentservice.entity;

/**
 * Access-control roles assigned to a {@link User}, used for JWT claims and
 * method-level security (e.g. {@code @PreAuthorize("hasRole('DOCTOR')")}).
 */
public enum Role {
    /** Full administrative access across all resources. */
    ADMIN,
    /** Manages their own availability and appointments. */
    DOCTOR,
    /** Books and manages their own appointments. */
    PATIENT
}
