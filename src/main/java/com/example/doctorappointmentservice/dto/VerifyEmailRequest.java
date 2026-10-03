package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for {@code POST /api/v1/auth/verify-email}. */
public record VerifyEmailRequest(

        @NotBlank(message = "Token is required")
        @Size(max = 128, message = "Invalid token")
        String token
) {}
