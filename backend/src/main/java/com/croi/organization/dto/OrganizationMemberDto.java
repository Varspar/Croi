package com.croi.organization.dto;

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
public class OrganizationMemberDto {

    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private String role;
    private Instant createdAt;
}
