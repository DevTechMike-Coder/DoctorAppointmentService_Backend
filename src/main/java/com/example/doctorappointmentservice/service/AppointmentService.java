package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.AppointmentDto;
import com.example.doctorappointmentservice.dto.BookAppointmentRequest;
import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.exception.SlotUnavailableException;
import com.example.doctorappointmentservice.repository.AppointmentRepository;
import com.example.doctorappointmentservice.repository.AvailabilitySlotRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentDto bookAppointment(Long patientId, BookAppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + patientId));

        AvailabilitySlot slot = availabilitySlotRepository.findByIdForUpdate(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.slotId()));

        if (slot.isBooked()) {
            throw new SlotUnavailableException("This slot is no longer available");
        }

        slot.setBooked(true);
        availabilitySlotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .slot(slot)
                .status(AppointmentStatus.PENDING)
                .reason(request.reason())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromEntity(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return AppointmentDto.fromEntity(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(AppointmentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findBySlot_DoctorProfile_Id(doctorId).stream()
                .map(AppointmentDto::fromEntity)
                .toList();
    }

    @Transactional
    public AppointmentDto updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        appointment.setStatus(newStatus);

        if (newStatus == AppointmentStatus.CANCELLED) {
            AvailabilitySlot slot = appointment.getSlot();
            slot.setBooked(false);
            availabilitySlotRepository.save(slot);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromEntity(updatedAppointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
}