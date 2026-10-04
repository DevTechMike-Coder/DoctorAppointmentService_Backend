package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.PracticeLocation;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.PracticeLocationRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages doctor profile data: browsing/searching doctors, and letting a
 * doctor create or update their own profile.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final PracticeLocationRepository practiceLocationRepository;

    /** Lists every doctor profile in the system. */
    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return toDtos(doctorProfileRepository.findAll());
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
        return toDto(profile);
    }

    /** Case-insensitive partial-match search of doctors by specialization. */
    @Transactional(readOnly = true)
    public List<DoctorDto> searchBySpecialty(String specialty) {
        return toDtos(doctorProfileRepository.findBySpecializationContainingIgnoreCase(specialty));
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

        // The display name lives on the User record; without this the submitted name was silently ignored.
        String newName = dto.fullName() == null ? "" : dto.fullName().trim();
        if (!newName.isEmpty() && !newName.equals(user.getFullName())) {
            if (newName.length() > 255) {
                throw new IllegalArgumentException("Name must be 255 characters or fewer");
            }
            user.setFullName(newName);
            userRepository.save(user);
        }

        profile.setSpecialization(dto.specialization());
        profile.setQualifications(dto.qualifications());
        profile.setBio(dto.bio());
        profile.setConsultationFee(dto.consultationFee());

        DoctorProfile saved = doctorProfileRepository.save(profile);
        return toDto(saved);
    }

    /** Maps one profile, attaching its primary workplace (if any). */
    private DoctorDto toDto(DoctorProfile profile) {
        PracticeLocation primary = practiceLocationRepository
                .findFirstByDoctorIdAndPrimaryLocationTrue(profile.getId())
                .orElse(null);
        return DoctorDto.fromEntity(profile, primary);
    }

    /** Maps many profiles with a single extra query for all their primary workplaces (no N+1). */
    private List<DoctorDto> toDtos(List<DoctorProfile> profiles) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        List<Long> ids = profiles.stream().map(DoctorProfile::getId).toList();
        Map<Long, PracticeLocation> primaryByDoctor = practiceLocationRepository.findPrimaryForDoctors(ids).stream()
                .collect(Collectors.toMap(l -> l.getDoctor().getId(), Function.identity(), (a, b) -> a));
        return profiles.stream()
                .map(p -> DoctorDto.fromEntity(p, primaryByDoctor.get(p.getId())))
                .toList();
    }
}