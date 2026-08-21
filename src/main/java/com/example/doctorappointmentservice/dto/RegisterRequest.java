package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email detected")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 8, message = "Password must be 8 characters")
        String password,

        @NotNull(message = "Role is required")
        String role
) {
}
