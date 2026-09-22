package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages doctor profile data: browsing/searching doctors, and letting a
 * doctor create or update their own profile.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    /** Lists every doctor profile in the system. */
    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(DoctorDto::fromEntity)
                .toList();
    }

    /**
     * @param id the doctor profile id
     * @return the matching doctor profile
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if no profile has that id
     */
    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + id));
        return DoctorDto.fromEntity(profile);
    }

    /** Case-insensitive partial-match search of doctors by specialization. */
    @Transactional(readOnly = true)
    public List<DoctorDto> searchBySpecialty(String specialty) {
        return doctorProfileRepository.findBySpecializationContainingIgnoreCase(specialty).stream()
                .map(DoctorDto::fromEntity)
                .toList();
    }

    /**
     * Creates a doctor profile for the given user if one doesn't exist yet,
     * otherwise updates the existing profile's editable fields.
     *
     * @param userId the account (with role DOCTOR) the profile belongs to
     * @param dto    the profile fields to save
     * @return the created/updated profile
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if the user doesn't exist
     */
    @Transactional
    public DoctorDto createOrUpdateProfile(Long userId, DoctorDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseGet(() -> DoctorProfile.builder().user(user).build());

        profile.setSpecialization(dto.specialization());
        profile.setQualifications(dto.qualifications());
        profile.setBio(dto.bio());
        profile.setConsultationFee(dto.consultationFee());

        DoctorProfile saved = doctorProfileRepository.save(profile);
        return DoctorDto.fromEntity(saved);
    }
}