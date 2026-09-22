package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import java.time.LocalDateTime;

/**
 * Read-facing view of an {@link com.example.doctorappointmentservice.entity.Appointment},
 * flattening the doctor and patient names alongside the slot's start/end times
 * so API clients don't need to traverse nested entities.
 */
public record AppointmentDto(
        Long id,
        Long doctorId,
        String doctorName,
        Long patientId,
        String patientName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        String reason,
        LocalDateTime createdAt
) {
    /**
     * Builds an {@link AppointmentDto} from an {@link Appointment} entity,
     * pulling doctor/patient display names off the associated slot and user.
     *
     * @param appointment the persisted appointment to convert
     * @return an immutable DTO suitable for returning from the API
     */
    public static AppointmentDto fromEntity(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getSlot().getDoctorProfile().getId(),
                appointment.getSlot().getDoctorProfile().getUser().getFullName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getCreatedAt()
        );
    }
}