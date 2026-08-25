package com.croi.voiceagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAgentDto {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String status;
    private String phoneNumber;
    private VoiceAgentConfigDto config;
    private Instant createdAt;
}
