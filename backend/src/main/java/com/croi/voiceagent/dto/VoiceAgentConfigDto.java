package com.croi.voiceagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAgentConfigDto {

    private String systemPrompt;
    private String tone;
    private Double temperature;
    private String model;
    private Integer maxDurationSeconds;
}
