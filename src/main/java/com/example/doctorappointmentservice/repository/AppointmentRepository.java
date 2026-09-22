package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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
}