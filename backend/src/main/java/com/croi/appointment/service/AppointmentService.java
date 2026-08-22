package com.croi.appointment.service;

import com.croi.appointment.dto.AppointmentDto;
import com.croi.appointment.dto.CreateAppointmentRequest;
import com.croi.appointment.entity.Appointment;
import com.croi.appointment.entity.AppointmentStatus;
import com.croi.appointment.repository.AppointmentRepository;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.common.exception.UnauthorizedException;
import com.croi.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int DEFAULT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;
    private final QueueService queueService;

    @Transactional
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : DEFAULT_DURATION_MINUTES;

        if (!availabilityService.isAvailable(request.getOrganizationId(), request.getAppointmentDate(), duration)) {
            throw new ValidationException(ErrorCode.APPOINTMENT_SLOT_UNAVAILABLE, ErrorMessages.APPOINTMENT_SLOT_UNAVAILABLE);
        }

        Appointment appointment = Appointment.builder()
                .organizationId(request.getOrganizationId())
                .patientName(request.getPatientName())
                .patientPhone(request.getPatientPhone())
                .appointmentDate(request.getAppointmentDate())
                .reason(request.getReason())
                .durationMinutes(duration)
                .status(AppointmentStatus.SCHEDULED.name())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        queueService.updateQueueStatus(request.getOrganizationId());

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByDate(UUID organizationId, LocalDate date) {
        return appointmentRepository.findByOrganizationIdAndAppointmentDateBetween(
                        organizationId, date.atStartOfDay(), date.atTime(LocalTime.MAX))
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByPhone(UUID organizationId, String patientPhone) {
        return appointmentRepository.findByOrganizationIdAndPatientPhone(organizationId, patientPhone)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public AppointmentDto cancelAppointment(UUID appointmentId, UUID organizationId) {
        Appointment appointment = findOwnedOrThrow(appointmentId, organizationId);
        appointment.setStatus(AppointmentStatus.CANCELLED.name());
        Appointment saved = appointmentRepository.save(appointment);
        queueService.updateQueueStatus(organizationId);
        return toDto(saved);
    }

    @Transactional
    public AppointmentDto markAsCompleted(UUID appointmentId, UUID organizationId) {
        Appointment appointment = findOwnedOrThrow(appointmentId, organizationId);
        appointment.setStatus(AppointmentStatus.COMPLETED.name());
        Appointment saved = appointmentRepository.save(appointment);
        queueService.updateQueueStatus(organizationId);
        return toDto(saved);
    }

    private Appointment findOwnedOrThrow(UUID appointmentId, UUID organizationId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPOINTMENT_NOT_FOUND, ErrorMessages.APPOINTMENT_NOT_FOUND));
        if (!appointment.getOrganizationId().equals(organizationId)) {
            // Matches the cross-workspace-ownership pattern used elsewhere (e.g. WorkspaceAdminController):
            // a 403, not a 404 that would leak "an appointment with this id exists somewhere".
            throw new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
        return appointment;
    }

    private AppointmentDto toDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .organizationId(appointment.getOrganizationId())
                .patientName(appointment.getPatientName())
                .patientPhone(appointment.getPatientPhone())
                .appointmentDate(appointment.getAppointmentDate())
                .durationMinutes(appointment.getDurationMinutes())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .calendarEventId(appointment.getCalendarEventId())
                .callRecordingId(appointment.getCallRecordingId())
                .confirmationCode(confirmationCode(appointment.getId()))
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    private String confirmationCode(UUID id) {
        return "APT" + id.toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
