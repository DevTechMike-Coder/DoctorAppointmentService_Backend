package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import java.time.LocalDateTime;

/**
 * Read-facing view of an {@link com.example.doctorappointmentservice.entity.AvailabilitySlot},
 * including the owning doctor's name for display purposes.
 */
public record AvailabilityDto(
        Long id,
        Long doctorId,
        String doctorName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isBooked
) {
    /**
     * Builds an {@link AvailabilityDto} from an {@link AvailabilitySlot} entity.
     *
     * @param slot the persisted slot to convert
     * @return an immutable DTO suitable for returning from the API
     */
    public static AvailabilityDto fromEntity(AvailabilitySlot slot) {
        return new AvailabilityDto(
                slot.getId(),
                slot.getDoctorProfile().getId(),
                slot.getDoctorProfile().getUser().getFullName(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.isBooked()
        );
    }
}