package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for a patient booking an appointment: the target slot and
 * an optional free-text reason for the visit.
 */
public record BookAppointmentRequest(
        @NotNull Long slotId,
        String reason
) {}