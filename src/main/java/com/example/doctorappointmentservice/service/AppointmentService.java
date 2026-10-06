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

/**
 * Core business logic for booking, viewing, and updating appointments.
 * Booking uses a pessimistic row lock on the target slot to prevent two
 * patients from booking the same slot concurrently.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final UserRepository userRepository;

    /**
     * Books an appointment for a patient against a specific availability slot.
     * Locks the slot row for the duration of the transaction so a concurrent
     * booking attempt on the same slot must wait and then fail cleanly.
     *
     * @param patientId id of the patient making the booking
     * @param request   the target slot id and optional reason for the visit
     * @return the newly created appointment
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if the patient or slot doesn't exist
     * @throws com.example.doctorappointmentservice.exception.SlotUnavailableException if the slot is already booked
     */
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

    /**
     * @param id the appointment id
     * @return the matching appointment
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if no appointment has that id
     */
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return AppointmentDto.fromEntity(appointment);
    }

    /** Lists every appointment booked by the given patient. */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(AppointmentDto::fromEntity)
                .toList();
    }

    /** Lists every appointment scheduled against the given doctor's slots. */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findBySlot_DoctorProfile_Id(doctorId).stream()
                .map(AppointmentDto::fromEntity)
                .toList();
    }

    /**
     * Updates an appointment's status. Transitioning to CANCELLED also
     * frees up the underlying slot so it can be booked again.
     *
     * @param appointmentId the appointment to update
     * @param newStatus the status to move it to
     * @return the updated appointment
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if the appointment doesn't exist
     */
    @Transactional
    public AppointmentDto updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        AppointmentStatus current = appointment.getStatus();
        if (current == newStatus) {
            return AppointmentDto.fromEntity(appointment); // idempotent, e.g. cancelling twice
        }
        // CANCELLED/COMPLETED are final. Without this, re-confirming a cancelled appointment leaves its
        // slot released, so a second patient could book a slot that already has a live appointment.
        if (current == AppointmentStatus.CANCELLED || current == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "A " + current.name().toLowerCase() + " appointment can no longer be changed");
        }
        if (newStatus == AppointmentStatus.PENDING) {
            throw new IllegalStateException("An appointment can't be moved back to pending");
        }
        if (newStatus == AppointmentStatus.COMPLETED && current != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed appointment can be marked completed");
        }

        appointment.setStatus(newStatus);

        if (newStatus == AppointmentStatus.CANCELLED) {
            AvailabilitySlot slot = appointment.getSlot();
            slot.setBooked(false);
            availabilitySlotRepository.save(slot);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromEntity(updatedAppointment);
    }

    /** Convenience wrapper that cancels an appointment and frees its slot. */
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
}