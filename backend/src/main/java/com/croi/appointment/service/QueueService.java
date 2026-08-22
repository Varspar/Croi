package com.croi.appointment.service;

import com.croi.appointment.dto.QueueStatusDto;
import com.croi.appointment.entity.Appointment;
import com.croi.appointment.entity.AppointmentStatus;
import com.croi.appointment.entity.QueueStatus;
import com.croi.appointment.repository.AppointmentRepository;
import com.croi.appointment.repository.QueueStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final AppointmentRepository appointmentRepository;
    private final QueueStatusRepository queueStatusRepository;

    @Transactional(readOnly = true)
    public int getCurrentQueueSize(UUID organizationId) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByOrganizationIdAndAppointmentDateBetweenAndStatus(
                organizationId, today.atStartOfDay(), today.atTime(LocalTime.MAX), AppointmentStatus.SCHEDULED.name()).size();
    }

    @Transactional(readOnly = true)
    public int getEstimatedWaitMinutes(UUID organizationId) {
        List<Appointment> appointments = appointmentRepository.findByOrganizationIdAndStatusOrderByAppointmentDateAsc(
                organizationId, AppointmentStatus.SCHEDULED.name());

        LocalDateTime now = LocalDateTime.now();
        int waitMinutes = appointments.stream()
                .filter(apt -> apt.getAppointmentDate().isAfter(now))
                .mapToInt(Appointment::getDurationMinutes)
                .sum();

        return Math.max(0, waitMinutes - 10);
    }

    private String classify(int currentPatientCount) {
        if (currentPatientCount > 15) return "busy";
        if (currentPatientCount >= 5) return "normal";
        return "slow";
    }

    @Transactional(readOnly = true)
    public QueueStatusDto getStatus(UUID organizationId) {
        int count = getCurrentQueueSize(organizationId);
        return QueueStatusDto.builder()
                .currentPatientCount(count)
                .estimatedWaitMinutes(getEstimatedWaitMinutes(organizationId))
                .status(classify(count))
                .build();
    }

    @Transactional
    public void updateQueueStatus(UUID organizationId) {
        int count = getCurrentQueueSize(organizationId);
        int waitMinutes = getEstimatedWaitMinutes(organizationId);
        String status = classify(count);

        QueueStatus queueStatus = queueStatusRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> QueueStatus.builder().organizationId(organizationId).build());

        queueStatus.setCurrentPatientCount(count);
        queueStatus.setEstimatedWaitMinutes(waitMinutes);
        queueStatus.setStatus(status);
        queueStatus.setLastUpdated(LocalDateTime.now());

        queueStatusRepository.save(queueStatus);
    }
}
