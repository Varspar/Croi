package com.croi.organization.service;

import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ConflictException;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.common.utils.ValidationUtils;
import com.croi.organization.dto.AddMemberRequest;
import com.croi.organization.dto.CreateOrganizationRequest;
import com.croi.organization.dto.OrganizationDto;
import com.croi.organization.dto.OrganizationMemberDto;
import com.croi.organization.dto.UpdateOrganizationRequest;
import com.croi.organization.entity.Organization;
import com.croi.organization.entity.OrganizationMember;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public OrganizationDto createOrganization(CreateOrganizationRequest request, UUID ownerId) {
        String baseSlug = ValidationUtils.slugify(request.getName());
        String slug = uniqueSlug(baseSlug);

        Organization organization = Organization.builder()
                .name(request.getName())
                .slug(slug)
                .ownerId(ownerId)
                .build();
        organization = organizationRepository.save(organization);

        OrganizationMember owner = OrganizationMember.builder()
                .organizationId(organization.getId())
                .userId(ownerId)
                .role("OWNER")
                .build();
        organizationMemberRepository.save(owner);

        return toDto(organization);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationById(UUID id) {
        return toDto(findOrganizationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getOrganizationsByUser(UUID userId) {
        return organizationMemberRepository.findByUserId(userId).stream()
                .map(member -> findOrganizationOrThrow(member.getOrganizationId()))
                .map(this::toDto)
                .toList();
    }

    public OrganizationDto updateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization organization = findOrganizationOrThrow(id);
        organization.setName(request.getName());
        if (request.getIsActive() != null) {
            organization.setIsActive(request.getIsActive());
        }
        return toDto(organizationRepository.save(organization));
    }

    public OrganizationMemberDto addMemberToOrganization(UUID organizationId, AddMemberRequest request) {
        findOrganizationOrThrow(organizationId);

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, request.getUserId())) {
            throw new ConflictException(ErrorCode.MEMBER_ALREADY_EXISTS, ErrorMessages.MEMBER_ALREADY_EXISTS);
        }

        OrganizationMember member = OrganizationMember.builder()
                .organizationId(organizationId)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        return toMemberDto(organizationMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberDto> getMembers(UUID organizationId) {
        findOrganizationOrThrow(organizationId);
        return organizationMemberRepository.findByOrganizationId(organizationId).stream()
                .map(this::toMemberDto)
                .toList();
    }

    private String uniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int suffix = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private Organization findOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZATION_NOT_FOUND, ErrorMessages.ORGANIZATION_NOT_FOUND));
    }

    private OrganizationDto toDto(Organization organization) {
        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .slug(organization.getSlug())
                .ownerId(organization.getOwnerId())
                .isActive(organization.getIsActive())
                .createdAt(organization.getCreatedAt())
                .build();
    }

    private OrganizationMemberDto toMemberDto(OrganizationMember member) {
        return OrganizationMemberDto.builder()
                .id(member.getId())
                .organizationId(member.getOrganizationId())
                .userId(member.getUserId())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }

}
