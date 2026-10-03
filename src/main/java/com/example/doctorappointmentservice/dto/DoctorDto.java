package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.DoctorProfile;
import java.math.BigDecimal;

/**
 * Read/write view of a {@link com.example.doctorappointmentservice.entity.DoctorProfile}.
 * Used both to display a doctor's public profile and as the request body
 * when a doctor creates or updates their own profile.
 */
public record DoctorDto(
        Long id,
        Long userId,
        String fullName,
        String specialization,
        String qualifications,
        String bio,
        BigDecimal consultationFee,
        String photoUrl
) {
    /**
     * Builds a {@link DoctorDto} from a {@link DoctorProfile} entity.
     *
     * @param profile the persisted doctor profile to convert
     * @return an immutable DTO suitable for returning from the API
     */
    public static DoctorDto fromEntity(DoctorProfile profile) {
        return new DoctorDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getSpecialization(),
                profile.getQualifications(),
                profile.getBio(),
                profile.getConsultationFee(),
                profile.getPhotoVersion() == null
                        ? null
                        : "/doctors/" + profile.getId() + "/photo?v=" + profile.getPhotoVersion()
        );
    }
}