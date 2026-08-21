package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.DoctorProfile;
import java.math.BigDecimal;

public record DoctorDto(
        Long id,
        Long userId,
        String fullName,
        String specialization,
        String qualifications,
        String bio,
        BigDecimal consultationFee
) {
    public static DoctorDto fromEntity(DoctorProfile profile) {
        return new DoctorDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getSpecialization(),
                profile.getQualifications(),
                profile.getBio(),
                profile.getConsultationFee()
        );
    }
}