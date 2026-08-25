package com.croi.voiceagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** All fields optional — only non-null ones are applied (partial update). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoiceAgentRequest {

    private String name;
    private String status;
    private String phoneNumber;
}
