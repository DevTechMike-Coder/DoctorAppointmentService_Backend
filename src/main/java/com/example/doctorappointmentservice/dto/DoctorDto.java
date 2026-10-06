package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.PracticeLocation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Read/write view of a {@link com.example.doctorappointmentservice.entity.DoctorProfile}.
 * Used both to display a doctor's public profile and as the request body
 * when a doctor creates or updates their own profile.
 */
public record DoctorDto(
        Long id,
        Long userId,
        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String fullName,
        @NotBlank(message = "Specialization is required")
        @Size(max = 255, message = "Specialization must be 255 characters or fewer")
        String specialization,
        @Size(max = 255, message = "Qualifications must be 255 characters or fewer")
        String qualifications,
        @Size(max = 255, message = "Bio must be 255 characters or fewer")
        String bio,
        @NotNull(message = "Consultation fee is required")
        @DecimalMin(value = "0.00", message = "Consultation fee can't be negative")
        @Digits(integer = 17, fraction = 2, message = "Consultation fee must have at most 2 decimal places")
        BigDecimal consultationFee,
        String photoUrl,
        /** City of the doctor's primary workplace; null if none (read-only, ignored on write). */
        String primaryCity,
        /** ISO 3166-1 alpha-2 country of the primary workplace; null if none (read-only, ignored on write). */
        String primaryCountry
) {
    /**
     * Builds a {@link DoctorDto} from a {@link DoctorProfile} entity.
     *
     * @param profile the persisted doctor profile to convert
     * @return an immutable DTO suitable for returning from the API
     */
    public static DoctorDto fromEntity(DoctorProfile profile) {
        return fromEntity(profile, null);
    }

    /**
     * Same as {@link #fromEntity(DoctorProfile)} but also fills in the primary workplace city/country.
     *
     * @param primary the doctor's primary location, or null if they have none
     */
    public static DoctorDto fromEntity(DoctorProfile profile, PracticeLocation primary) {
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
                        : "/doctors/" + profile.getId() + "/photo?v=" + profile.getPhotoVersion(),
                primary == null ? null : primary.getCity(),
                primary == null ? null : primary.getCountry()
        );
    }
}