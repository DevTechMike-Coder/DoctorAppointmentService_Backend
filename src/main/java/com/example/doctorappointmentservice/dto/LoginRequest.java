package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/auth/login}: email + password credentials.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email detected")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
