package com.croi.organization.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.organization.dto.WorkspaceBrandingDto;
import com.croi.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Unauthenticated endpoints for the public guest-chat widget. Only ever exposes
 * branding fields that are safe to show to an anonymous visitor — never config,
 * documents, or anything else workspace-internal.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspacePublicController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}/branding")
    public ResponseEntity<ApiResponse<WorkspaceBrandingDto>> branding(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getBranding(id)));
    }
}
