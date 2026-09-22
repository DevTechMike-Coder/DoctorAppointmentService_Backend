package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AvailabilitySlot} entities.
 */
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    /** Finds unbooked slots for a doctor whose start time falls within the given window. */
    List<AvailabilitySlot> findByDoctorProfile_IdAndIsBookedFalseAndStartTimeBetween(Long doctorProfileId, LocalDateTime from, LocalDateTime to);

    /**
     * Fetches a slot with a pessimistic write lock so concurrent booking
     * attempts on the same slot are serialized at the database level,
     * preventing double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvailabilitySlot s WHERE s.id = :id")
    Optional<AvailabilitySlot> findByIdForUpdate(Long id);
}
