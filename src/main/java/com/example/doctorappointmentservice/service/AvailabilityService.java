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

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailableSlots(Long doctorId, LocalDateTime from, LocalDateTime to) {
        return availabilitySlotRepository
                .findByDoctorProfile_IdAndIsBookedFalseAndStartTimeBetween(doctorId, from, to)
                .stream()
                .map(AvailabilityDto::fromEntity)
                .toList();
    }

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