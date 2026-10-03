package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link ProfilePhoto}; the primary key is the owning user's id.
 */
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {
}
