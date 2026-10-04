package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.PracticeLocationDto;
import com.example.doctorappointmentservice.dto.PracticeLocationRequest;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.PracticeLocation;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.PracticeLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeLocationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long DOCTOR_ID = 7L;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;
    @Mock
    private PracticeLocationRepository locationRepository;
    @InjectMocks
    private PracticeLocationService service;

    private static DoctorProfile profile() {
        return DoctorProfile.builder().id(DOCTOR_ID).build();
    }

    private static PracticeLocationRequest request(boolean primary, BigDecimal lat, BigDecimal lng) {
        return new PracticeLocationRequest("City Clinic", "  12 Marina Rd  ", null, "Lagos", "Lagos",
                "100001", "ng", lat, lng, primary);
    }

    @Test
    void firstLocationIsPrimaryAndInputIsNormalised() {
        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.countByDoctorId(DOCTOR_ID)).thenReturn(0L);
        when(locationRepository.save(any(PracticeLocation.class))).thenAnswer(inv -> inv.getArgument(0));

        PracticeLocationDto dto = service.create(USER_ID, request(false, null, null));

        assertThat(dto.primary()).isTrue();
        assertThat(dto.country()).isEqualTo("NG");
        assertThat(dto.addressLine1()).isEqualTo("12 Marina Rd");
        assertThat(dto.addressLine2()).isNull();
        verify(locationRepository, never()).clearPrimary(any());
    }

    @Test
    void newPrimaryClearsTheOldOne() {
        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.countByDoctorId(DOCTOR_ID)).thenReturn(1L);
        when(locationRepository.save(any(PracticeLocation.class))).thenAnswer(inv -> inv.getArgument(0));

        PracticeLocationDto dto = service.create(USER_ID, request(true, null, null));

        assertThat(dto.primary()).isTrue();
        verify(locationRepository).clearPrimary(DOCTOR_ID);
    }

    @Test
    void rejectsLatitudeWithoutLongitude() {
        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.countByDoctorId(DOCTOR_ID)).thenReturn(0L);

        assertThatThrownBy(() -> service.create(USER_ID, request(false, new BigDecimal("6.5244"), null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(locationRepository, never()).save(any(PracticeLocation.class));
    }

    @Test
    void enforcesPerDoctorCap() {
        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.countByDoctorId(DOCTOR_ID))
                .thenReturn((long) PracticeLocationService.MAX_LOCATIONS_PER_DOCTOR);

        assertThatThrownBy(() -> service.create(USER_ID, request(false, null, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUpdateAnotherDoctorsLocation() {
        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.findByIdAndDoctorId(99L, DOCTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(USER_ID, 99L, request(false, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(locationRepository, never()).save(any(PracticeLocation.class));
    }

    @Test
    void deletingThePrimaryPromotesTheOldestRemaining() {
        PracticeLocation primary = new PracticeLocation();
        primary.setPrimaryLocation(true);
        PracticeLocation next = new PracticeLocation();

        when(doctorProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
        when(locationRepository.findByIdAndDoctorId(5L, DOCTOR_ID)).thenReturn(Optional.of(primary));
        when(locationRepository.findFirstByDoctorIdOrderByIdAsc(DOCTOR_ID)).thenReturn(Optional.of(next));

        service.delete(USER_ID, 5L);

        verify(locationRepository).delete(primary);
        assertThat(next.isPrimaryLocation()).isTrue();
    }
}
