package com.croi.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {

    private UUID id;
    private UUID organizationId;
    private String patientName;
    private String patientPhone;
    private LocalDateTime appointmentDate;
    private Integer durationMinutes;
    private String reason;
    private String status;
    private String calendarEventId;
    private UUID callRecordingId;
    /** e.g. "APT1A2B3C4D" — the id's first 8 hex chars, uppercased, for a human to read back on a call. */
    private String confirmationCode;
    private LocalDateTime createdAt;
}
