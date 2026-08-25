package com.croi.voiceagent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoiceAgentRequest {

    @NotNull
    private UUID organizationId;

    @NotBlank
    private String name;

    private String phoneNumber;
}
