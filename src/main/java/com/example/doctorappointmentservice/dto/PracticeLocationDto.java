package com.example.doctorappointmentservice.dto;

import com.example.doctorappointmentservice.entity.PracticeLocation;

import java.math.BigDecimal;

/** Read view of a {@link PracticeLocation}; returned to the owning doctor and to patients browsing. */
public record PracticeLocationDto(
        Long id,
        String facilityName,
        String addressLine1,
        String addressLine2,
        String city,
        String stateRegion,
        String postalCode,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean primary
) {
    public static PracticeLocationDto fromEntity(PracticeLocation l) {
        return new PracticeLocationDto(
                l.getId(),
                l.getFacilityName(),
                l.getAddressLine1(),
                l.getAddressLine2(),
                l.getCity(),
                l.getStateRegion(),
                l.getPostalCode(),
                l.getCountry(),
                l.getLatitude(),
                l.getLongitude(),
                l.isPrimaryLocation()
        );
    }
}
