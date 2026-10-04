package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.entity.PracticeLocation;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import com.example.doctorappointmentservice.repository.PracticeLocationRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PracticeLocationRepository practiceLocationRepository;
    @InjectMocks
    private DoctorService service;

    private static DoctorProfile doctor(long id) {
        User user = User.builder().id(100 + id).fullName("Dr. " + id).build();
        return DoctorProfile.builder()
                .id(id).user(user).specialization("Cardiology")
                .consultationFee(BigDecimal.TEN).build();
    }

    private static PracticeLocation primaryAt(DoctorProfile doctor, String city, String country) {
        PracticeLocation l = new PracticeLocation();
        l.setDoctor(doctor);
        l.setCity(city);
        l.setCountry(country);
        l.setPrimaryLocation(true);
        return l;
    }

    @Test
    void listAttachesPrimaryCityWithASingleLocationQuery() {
        DoctorProfile withLocation = doctor(1);
        DoctorProfile without = doctor(2);
        when(doctorProfileRepository.findAll()).thenReturn(List.of(withLocation, without));
        when(practiceLocationRepository.findPrimaryForDoctors(anyCollection()))
                .thenReturn(List.of(primaryAt(withLocation, "Lagos", "NG")));

        List<DoctorDto> result = service.getAllDoctors();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).primaryCity()).isEqualTo("Lagos");
        assertThat(result.get(0).primaryCountry()).isEqualTo("NG");
        assertThat(result.get(1).primaryCity()).isNull();
        verify(practiceLocationRepository).findPrimaryForDoctors(List.of(1L, 2L));
        verify(practiceLocationRepository, never()).findFirstByDoctorIdAndPrimaryLocationTrue(anyLong());
    }

    @Test
    void emptyListSkipsTheLocationQuery() {
        when(doctorProfileRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllDoctors()).isEmpty();
        verify(practiceLocationRepository, never()).findPrimaryForDoctors(anyCollection());
    }
}
