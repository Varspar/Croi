package com.croi.conversation.dto;

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
public class ConversationDto {

    private UUID id;
    private UUID organizationId;
    private String customerName;
    private String customerEmail;
    private String status;
    private Boolean isEscalated;
    private Instant createdAt;
}
