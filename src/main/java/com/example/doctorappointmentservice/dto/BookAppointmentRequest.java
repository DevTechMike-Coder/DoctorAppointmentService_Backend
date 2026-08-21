package com.example.doctorappointmentservice.dto;

import jakarta.validation.constraints.NotNull;

public record BookAppointmentRequest(
        @NotNull Long slotId,
        String reason
) {}