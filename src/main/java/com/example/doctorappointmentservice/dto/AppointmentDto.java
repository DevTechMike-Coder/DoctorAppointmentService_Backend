package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import java.time.LocalDateTime;

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