package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request body for creating or updating one of the authenticated doctor's
 * workplace locations. Latitude and longitude are optional but must be supplied together.
 */
public record PracticeLocationRequest(
        @NotBlank(message = "Facility name is required")
        @Size(max = 150, message = "Facility name must be 150 characters or fewer")
        String facilityName,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 200, message = "Address line 1 must be 200 characters or fewer")
        String addressLine1,

        @Size(max = 200, message = "Address line 2 must be 200 characters or fewer")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be 100 characters or fewer")
        String city,

        @Size(max = 100, message = "State/region must be 100 characters or fewer")
        String stateRegion,

        @Size(max = 20, message = "Postal code must be 20 characters or fewer")
        @Pattern(regexp = "^[A-Za-z0-9 \\-]*$", message = "Postal code may only contain letters, digits, spaces and hyphens")
        String postalCode,

        @NotBlank(message = "Country is required")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country must be a 2-letter ISO code (e.g. NG)")
        String country,

        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        /** Marks this as the doctor's primary location. The first location is always primary. */
        boolean primary
) {
}
