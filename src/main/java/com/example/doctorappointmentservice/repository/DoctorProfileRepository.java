package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link DoctorProfile} entities.
 */
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    /** Finds the doctor profile belonging to a given user account, if any. */
    Optional<DoctorProfile> findByUserId(Long userId);

    /** Case-insensitive partial-match search of doctors by specialization. */
    List<DoctorProfile> findBySpecializationContainingIgnoreCase(String specialization);
}