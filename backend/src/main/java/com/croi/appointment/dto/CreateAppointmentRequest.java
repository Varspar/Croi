package com.croi.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull
    private UUID organizationId;

    @NotBlank
    private String patientName;

    @NotBlank
    private String patientPhone;

    @NotNull
    @Future
    private LocalDateTime appointmentDate;

    private String reason;

    @Positive
    private Integer durationMinutes;
}
