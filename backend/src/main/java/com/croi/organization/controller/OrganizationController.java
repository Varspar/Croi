package com.croi.organization.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.organization.dto.AddMemberRequest;
import com.croi.organization.dto.CreateOrganizationRequest;
import com.croi.organization.dto.OrganizationDto;
import com.croi.organization.dto.OrganizationMemberDto;
import com.croi.organization.dto.UpdateOrganizationRequest;
import com.croi.organization.service.OrganizationService;
import com.croi.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationDto>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.createOrganization(request, principal.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationDto>>> listMyOrganizations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getOrganizationsByUser(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationDto>> getOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getOrganizationById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationDto>> updateOrganization(
            @PathVariable UUID id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.updateOrganization(id, request)));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<OrganizationMemberDto>> addMember(
            @PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.addMemberToOrganization(id, request)));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberDto>>> listMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getMembers(id)));
    }
}
