package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findBySlot_DoctorProfile_Id(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);
}