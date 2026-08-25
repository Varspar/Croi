package com.croi.voice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCallRecordingRequest {

    @NotNull
    private UUID callId;

    /** One of {@link com.croi.appointment.entity.CallOutcome}'s names. */
    private String outcome;

    /** Total call duration in seconds. */
    private Integer duration;

    private String notes;
}
