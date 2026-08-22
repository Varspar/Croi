package com.croi.appointment.controller;

import com.croi.appointment.dto.AppointmentDto;
import com.croi.appointment.dto.CreateAppointmentRequest;
import com.croi.appointment.dto.QueueStatusDto;
import com.croi.appointment.service.AppointmentService;
import com.croi.appointment.service.AvailabilityService;
import com.croi.appointment.service.QueueService;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.dto.ApiResponse;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.UnauthorizedException;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business-logic layer only — no voice/telephony integration yet. Every endpoint
 * requires an authenticated, org-member caller (JWT via JwtFilter, same as the
 * rest of the app); there is no unauthenticated path here.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;
    private final QueueService queueService;
    private final OrganizationMemberRepository membershipRepository;

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<AppointmentDto>> scheduleAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(request.getOrganizationId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.createAppointment(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getAppointmentsByDate(
            @RequestParam UUID organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.getAppointmentsByDate(organizationId, date)));
    }

    @GetMapping("/by-phone")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getAppointmentsByPhone(
            @RequestParam UUID organizationId,
            @RequestParam String patientPhone,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.getAppointmentsByPhone(organizationId, patientPhone)));
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<java.time.LocalTime>>> getAvailableSlots(
            @RequestParam UUID organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(availabilityService.getAvailableSlots(organizationId, date)));
    }

    @GetMapping("/queue-status")
    public ResponseEntity<ApiResponse<QueueStatusDto>> getQueueStatus(
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        queueService.updateQueueStatus(organizationId);
        return ResponseEntity.ok(ApiResponse.ok(queueService.getStatus(organizationId)));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.cancelAppointment(appointmentId, organizationId)));
    }

    @PutMapping("/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<AppointmentDto>> completeAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.markAsCompleted(appointmentId, organizationId)));
    }

    private void requireMember(UUID organizationId, UUID userId) {
        if (!membershipRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
    }
}
