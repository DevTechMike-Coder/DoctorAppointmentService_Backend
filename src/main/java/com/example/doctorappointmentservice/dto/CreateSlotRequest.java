// CreateSlotRequest.java
package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Request body for a doctor/admin creating a new {@code AvailabilitySlot}.
 * Both timestamps are required and validated to be in the future.
 */
public record CreateSlotRequest(
        @NotNull(message = "startTime is required")
        @Future(message = "startTime must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "endTime is required")
        @Future(message = "endTime must be in the future")
        LocalDateTime endTime
) {
}