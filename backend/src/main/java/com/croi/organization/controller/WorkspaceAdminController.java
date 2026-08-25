package com.croi.organization.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.UnauthorizedException;
import com.croi.knowledge.dto.DocumentDto;
import com.croi.knowledge.service.DocumentService;
import com.croi.organization.entity.OrganizationMember;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.organization.service.OrganizationService;
import com.croi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class WorkspaceAdminController {
    private final DocumentService documentService;
    private final OrganizationService organizationService;
    private final OrganizationMemberRepository membershipRepository;

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<DocumentDto>> upload(@PathVariable UUID workspaceId, @RequestParam String title,
            @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserPrincipal principal) {
        requireOwner(workspaceId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(documentService.uploadDocument(workspaceId, title, file)));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> documents(@PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(workspaceId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(documentService.getDocumentsByOrganization(workspaceId)));
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID workspaceId, @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireOwner(workspaceId, principal.getId());
        DocumentDto document = documentService.getDocument(documentId);
        if (!workspaceId.equals(document.getOrganizationId())) throw new UnauthorizedException("Document does not belong to this workspace");
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Document deleted"));
    }

    @PostMapping("/documents/{documentId}/embeddings/generate")
    public ResponseEntity<ApiResponse<DocumentDto>> regenerate(@PathVariable UUID workspaceId, @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireOwner(workspaceId, principal.getId());
        DocumentDto document = documentService.getDocument(documentId);
        if (!workspaceId.equals(document.getOrganizationId())) throw new UnauthorizedException("Document does not belong to this workspace");
        return ResponseEntity.ok(ApiResponse.ok(documentService.regenerateEmbeddings(documentId)));
    }

    @PostMapping("/embeddings/generate")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> regenerateAll(@PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireOwner(workspaceId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(documentService.getDocumentsByOrganization(workspaceId).stream()
                .map(document -> documentService.regenerateEmbeddings(document.getId())).toList()));
    }

    private void requireMember(UUID workspaceId, UUID userId) {
        if (!membershipRepository.existsByOrganizationIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, "You are not a workspace member");
        }
    }
    private void requireOwner(UUID workspaceId, UUID userId) {
        OrganizationMember member = membershipRepository.findByOrganizationIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, "You are not a workspace member"));
        if (!"OWNER".equals(member.getRole())) {
            throw new UnauthorizedException(ErrorCode.WORKSPACE_OWNER_ONLY, "Only workspace owners can manage this setting");
        }
    }
}
