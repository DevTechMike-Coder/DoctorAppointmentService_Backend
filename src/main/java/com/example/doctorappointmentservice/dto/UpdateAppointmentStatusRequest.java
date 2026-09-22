// UpdateAppointmentStatusRequest.java
package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for a doctor/admin updating an appointment's status
 * (e.g. confirming, cancelling, or completing it).
 */
public record UpdateAppointmentStatusRequest(
        @NotNull(message = "status is required")
        AppointmentStatus status
) {
}