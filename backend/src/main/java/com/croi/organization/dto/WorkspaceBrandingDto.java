package com.croi.organization.dto;

import lombok.Builder;

/** Public-safe workspace info for the unauthenticated guest chat widget — name/branding only. */
@Builder
public record WorkspaceBrandingDto(String name, String logo, String primaryColor, String description) {
}
