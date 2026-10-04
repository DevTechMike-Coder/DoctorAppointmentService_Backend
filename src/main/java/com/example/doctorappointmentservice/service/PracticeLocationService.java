package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.PracticeLocationDto;
import com.example.doctorappointmentservice.dto.PracticeLocationRequest;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.PracticeLocation;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.PracticeLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lets a doctor manage their workplace locations and lets anyone read them.
 * Writes always resolve the doctor from the authenticated user id (never from the
 * request) and look locations up scoped to that doctor, so one doctor can't touch another's rows.
 */
@Service
@RequiredArgsConstructor
public class PracticeLocationService {

    static final int MAX_LOCATIONS_PER_DOCTOR = 10;

    /** Control characters and angle brackets have no place in an address. */
    private static final Pattern FORBIDDEN_CHARS = Pattern.compile("[\\p{Cntrl}<>]");

    private final DoctorProfileRepository doctorProfileRepository;
    private final PracticeLocationRepository locationRepository;

    /** Public: locations of the doctor with the given doctor-profile id. */
    @Transactional(readOnly = true)
    public List<PracticeLocationDto> listForDoctor(Long doctorProfileId) {
        if (!doctorProfileRepository.existsById(doctorProfileId)) {
            throw new ResourceNotFoundException("Doctor profile not found with id: " + doctorProfileId);
        }
        return locationRepository.findAllForDoctor(doctorProfileId).stream()
                .map(PracticeLocationDto::fromEntity)
                .toList();
    }

    /** The authenticated doctor's own locations (empty if they haven't saved a profile yet). */
    @Transactional(readOnly = true)
    public List<PracticeLocationDto> listMine(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .map(p -> locationRepository.findAllForDoctor(p.getId()).stream()
                        .map(PracticeLocationDto::fromEntity)
                        .toList())
                .orElse(List.of());
    }

    /**
     * @throws IllegalStateException    if the doctor has no profile yet or has reached the location cap
     * @throws IllegalArgumentException if the address fields are invalid
     */
    @Transactional
    public PracticeLocationDto create(Long userId, PracticeLocationRequest req) {
        DoctorProfile profile = requireProfile(userId);

        long existing = locationRepository.countByDoctorId(profile.getId());
        if (existing >= MAX_LOCATIONS_PER_DOCTOR) {
            throw new IllegalStateException("You can add at most " + MAX_LOCATIONS_PER_DOCTOR + " locations.");
        }

        PracticeLocation location = new PracticeLocation();
        location.setDoctor(profile);
        apply(location, req);

        boolean makePrimary = existing == 0 || req.primary();
        if (makePrimary && existing > 0) {
            locationRepository.clearPrimary(profile.getId());
        }
        location.setPrimaryLocation(makePrimary);

        return PracticeLocationDto.fromEntity(locationRepository.save(location));
    }

    /**
     * Updates one of the doctor's own locations. Setting {@code primary=true} moves the primary flag here;
     * {@code primary=false} on the current primary is ignored (promote another location instead).
     *
     * @throws ResourceNotFoundException if the location doesn't exist or isn't this doctor's
     */
    @Transactional
    public PracticeLocationDto update(Long userId, Long locationId, PracticeLocationRequest req) {
        DoctorProfile profile = requireProfile(userId);
        PracticeLocation location = findOwned(locationId, profile.getId());

        apply(location, req);

        if (req.primary() && !location.isPrimaryLocation()) {
            locationRepository.clearPrimary(profile.getId());
            location.setPrimaryLocation(true);
        }

        return PracticeLocationDto.fromEntity(locationRepository.save(location));
    }

    /** Deletes one of the doctor's own locations; if it was primary, the oldest remaining one is promoted. */
    @Transactional
    public void delete(Long userId, Long locationId) {
        DoctorProfile profile = requireProfile(userId);
        PracticeLocation location = findOwned(locationId, profile.getId());
        boolean wasPrimary = location.isPrimaryLocation();

        locationRepository.delete(location);
        locationRepository.flush();

        if (wasPrimary) {
            locationRepository.findFirstByDoctorIdOrderByIdAsc(profile.getId())
                    .ifPresent(next -> next.setPrimaryLocation(true));
        }
    }

    private DoctorProfile requireProfile(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Save your profile before adding a location."));
    }

    /** 404 (not 403) for someone else's location so ids can't be probed. */
    private PracticeLocation findOwned(Long locationId, Long doctorProfileId) {
        return locationRepository.findByIdAndDoctorId(locationId, doctorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));
    }

    private void apply(PracticeLocation target, PracticeLocationRequest req) {
        if ((req.latitude() == null) != (req.longitude() == null)) {
            throw new IllegalArgumentException("Latitude and longitude must be provided together.");
        }
        target.setFacilityName(clean(req.facilityName()));
        target.setAddressLine1(clean(req.addressLine1()));
        target.setAddressLine2(clean(req.addressLine2()));
        target.setCity(clean(req.city()));
        target.setStateRegion(clean(req.stateRegion()));
        target.setPostalCode(clean(req.postalCode()));
        target.setCountry(req.country().trim().toUpperCase(Locale.ROOT));
        target.setLatitude(req.latitude());
        target.setLongitude(req.longitude());
    }

    /** Trims, maps blank to null, and rejects control characters / angle brackets. */
    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (FORBIDDEN_CHARS.matcher(trimmed).find()) {
            throw new IllegalArgumentException("Address fields contain invalid characters.");
        }
        return trimmed;
    }
}
