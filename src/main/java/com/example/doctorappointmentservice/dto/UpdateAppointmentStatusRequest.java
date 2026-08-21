// UpdateAppointmentStatusRequest.java
package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "status is required")
        AppointmentStatus status
) {
}