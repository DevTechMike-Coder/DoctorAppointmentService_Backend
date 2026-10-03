package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request body for {@code POST /api/v1/auth/resend-verification}. */
public record ResendVerificationRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email detected")
        String email
) {}
