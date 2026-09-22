package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.AvailabilityDto;
import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.AvailabilitySlotRepository;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages a doctor's bookable time slots: listing open slots for patients,
 * and creating/deleting slots for doctors and admins.
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    /**
     * Lists a doctor's unbooked slots whose start time falls within the
     * given window, for patients browsing availability.
     */
    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailableSlots(Long doctorId, LocalDateTime from, LocalDateTime to) {
        return availabilitySlotRepository
                .findByDoctorProfile_IdAndIsBookedFalseAndStartTimeBetween(doctorId, from, to)
                .stream()
                .map(AvailabilityDto::fromEntity)
                .toList();
    }

    /**
     * Creates a new bookable slot for a doctor.
     *
     * @param doctorId  the doctor profile the slot belongs to
     * @param startTime slot start time
     * @param endTime   slot end time (must be after startTime)
     * @return the newly created slot
     * @throws IllegalArgumentException if endTime is not after startTime
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if the doctor doesn't exist
     */
    @Transactional
    public AvailabilityDto createSlot(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        AvailabilitySlot slot = AvailabilitySlot.builder()
                .doctorProfile(doctorProfile)
                .startTime(startTime)
                .endTime(endTime)
                .isBooked(false)
                .build();

        AvailabilitySlot savedSlot = availabilitySlotRepository.save(slot);
        return AvailabilityDto.fromEntity(savedSlot);
    }

    /**
     * Deletes a slot that has not yet been booked.
     *
     * @param slotId the slot to delete
     * @throws com.example.doctorappointmentservice.exception.ResourceNotFoundException if the slot doesn't exist
     * @throws IllegalStateException if the slot is already booked
     */
    @Transactional
    public void deleteSlot(Long slotId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));

        if (slot.isBooked()) {
            throw new IllegalStateException("Cannot delete a slot that is already booked");
        }

        availabilitySlotRepository.delete(slot);
    }
}