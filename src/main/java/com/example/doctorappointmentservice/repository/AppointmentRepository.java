package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Appointment} entities.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Finds all appointments booked by the given patient. */
    List<Appointment> findByPatientId(Long patientId);

    /** Finds all appointments belonging to slots owned by the given doctor. */
    List<Appointment> findBySlot_DoctorProfile_Id(Long doctorId);

    /** Finds all appointments currently in the given status. */
    List<Appointment> findByStatus(AppointmentStatus status);

    /** Loads an appointment with a write lock so two participants joining at once create only one room. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdForUpdate(Long id);
}
