package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(DoctorDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + id));
        return DoctorDto.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> searchBySpecialty(String specialty) {
        return doctorProfileRepository.findBySpecializationContainingIgnoreCase(specialty).stream()
                .map(DoctorDto::fromEntity)
                .toList();
    }

    @Transactional
    public DoctorDto createOrUpdateProfile(Long userId, DoctorDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseGet(() -> DoctorProfile.builder().user(user).build());

        profile.setSpecialization(dto.specialization());
        profile.setQualifications(dto.qualifications());
        profile.setBio(dto.bio());
        profile.setConsultationFee(dto.consultationFee());

        DoctorProfile saved = doctorProfileRepository.save(profile);
        return DoctorDto.fromEntity(saved);
    }
}