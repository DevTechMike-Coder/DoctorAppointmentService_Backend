package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.ProfilePhoto;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.ProfilePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Stores, serves and removes doctors' profile photos. Uploads are always attached
 * to the authenticated user's own profile (never an id from the request), so there
 * is no way to overwrite someone else's photo.
 */
@Service
@RequiredArgsConstructor
public class DoctorPhotoService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final ProfilePhotoRepository photoRepository;

    /**
     * @throws IllegalStateException    if the doctor hasn't saved a profile yet
     * @throws IllegalArgumentException if the file isn't an acceptable image
     */
    @Transactional
    public DoctorDto upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No image provided.");
        }

        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Save your profile before adding a photo."));

        byte[] processed;
        try {
            processed = ProfileImageProcessor.toAvatarJpeg(file.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Couldn't read the uploaded file.");
        }

        ProfilePhoto photo = photoRepository.findById(userId).orElseGet(() -> new ProfilePhoto(userId));
        photo.setContentType("image/jpeg");
        photo.setData(processed);
        photo.setUpdatedAt(LocalDateTime.now());
        photoRepository.save(photo);

        profile.setPhotoVersion(System.currentTimeMillis());
        return DoctorDto.fromEntity(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorDto remove(Long userId) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));
        photoRepository.deleteById(userId);
        profile.setPhotoVersion(null);
        return DoctorDto.fromEntity(doctorProfileRepository.save(profile));
    }

    /** @param doctorId the doctor *profile* id (same id the public doctor pages use) */
    @Transactional(readOnly = true)
    public ProfilePhoto getByDoctorId(Long doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));
        return photoRepository.findById(profile.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));
    }
}
