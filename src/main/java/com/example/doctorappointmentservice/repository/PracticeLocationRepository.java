package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.PracticeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PracticeLocation}. Every lookup of a single
 * location is scoped by the owning doctor profile id so callers can't reach
 * another doctor's rows by guessing ids.
 */
public interface PracticeLocationRepository extends JpaRepository<PracticeLocation, Long> {

    /** A doctor's locations, primary first, then oldest first. */
    @Query("select l from PracticeLocation l where l.doctor.id = :doctorId "
            + "order by l.primaryLocation desc, l.id asc")
    List<PracticeLocation> findAllForDoctor(@Param("doctorId") Long doctorId);

    /** Owner-scoped lookup; empty when the location exists but belongs to someone else. */
    @Query("select l from PracticeLocation l where l.id = :id and l.doctor.id = :doctorId")
    Optional<PracticeLocation> findByIdAndDoctorId(@Param("id") Long id, @Param("doctorId") Long doctorId);

    /** Oldest location of a doctor; used to promote a new primary after the primary is deleted. */
    Optional<PracticeLocation> findFirstByDoctorIdOrderByIdAsc(Long doctorId);

    long countByDoctorId(Long doctorId);

    /** The primary location of one doctor, if they have any locations. */
    Optional<PracticeLocation> findFirstByDoctorIdAndPrimaryLocationTrue(Long doctorId);

    /** Primary locations for many doctors in one query (avoids N+1 when listing doctors). */
    @Query("select l from PracticeLocation l join fetch l.doctor "
            + "where l.primaryLocation = true and l.doctor.id in :doctorIds")
    List<PracticeLocation> findPrimaryForDoctors(@Param("doctorIds") Collection<Long> doctorIds);

    /** Clears the primary flag on all of a doctor's locations (executed immediately via flush). */
    @Modifying(flushAutomatically = true)
    @Query("update PracticeLocation l set l.primaryLocation = false "
            + "where l.doctor.id = :doctorId and l.primaryLocation = true")
    void clearPrimary(@Param("doctorId") Long doctorId);
}
