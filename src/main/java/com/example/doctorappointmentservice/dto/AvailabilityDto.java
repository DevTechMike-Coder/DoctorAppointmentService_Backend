package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import java.time.LocalDateTime;

public record AvailabilityDto(
        Long id,
        Long doctorId,
        String doctorName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isBooked
) {
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