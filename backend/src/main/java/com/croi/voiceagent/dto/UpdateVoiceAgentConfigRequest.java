package com.croi.voiceagent.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoiceAgentConfigRequest {

    @NotBlank
    private String systemPrompt;

    @NotBlank
    private String tone;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private Double temperature;

    @NotBlank
    private String model;

    @NotNull
    @Positive
    private Integer maxDurationSeconds;
}
