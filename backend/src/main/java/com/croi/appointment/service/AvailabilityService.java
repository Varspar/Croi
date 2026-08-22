package com.croi.appointment.service;

import com.croi.appointment.entity.Appointment;
import com.croi.appointment.entity.AppointmentStatus;
import com.croi.appointment.entity.AvailabilityRule;
import com.croi.appointment.repository.AppointmentRepository;
import com.croi.appointment.repository.AvailabilityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID organizationId, LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();

        int dayOfWeek = date.getDayOfWeek().getValue() - 1; // 0 = Monday, matching AvailabilityRule.dayOfWeek
        Optional<AvailabilityRule> rule = availabilityRuleRepository.findByOrganizationIdAndDayOfWeek(organizationId, dayOfWeek);
        if (rule.isEmpty() || !Boolean.TRUE.equals(rule.get().getIsActive())) {
            return availableSlots;
        }

        AvailabilityRule availRule = rule.get();
        int appointmentDuration = availRule.getAppointmentDuration();
        int bufferTime = availRule.getBufferTime();

        List<Appointment> dayAppointments = appointmentRepository.findByOrganizationIdAndAppointmentDateBetweenAndStatus(
                organizationId, date.atStartOfDay(), date.atTime(LocalTime.MAX), AppointmentStatus.SCHEDULED.name());

        LocalTime currentTime = availRule.getStartTime();
        LocalTime endTime = availRule.getEndTime();
        while (!currentTime.plusMinutes(appointmentDuration).isAfter(endTime)) {
            LocalDateTime slotStart = date.atTime(currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(appointmentDuration);

            boolean isAvailable = dayAppointments.stream().noneMatch(existing -> overlaps(
                    slotStart, slotEnd, existing.getAppointmentDate(), existing.getAppointmentDate().plusMinutes(existing.getDurationMinutes())));

            if (isAvailable) {
                availableSlots.add(currentTime);
            }

            currentTime = currentTime.plusMinutes((long) appointmentDuration + bufferTime);
        }

        return availableSlots;
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(UUID organizationId, LocalDateTime appointmentDateTime, int durationMinutes) {
        LocalDateTime proposedEnd = appointmentDateTime.plusMinutes(durationMinutes);
        LocalDate day = appointmentDateTime.toLocalDate();

        List<Appointment> dayAppointments = appointmentRepository.findByOrganizationIdAndAppointmentDateBetweenAndStatus(
                organizationId, day.atStartOfDay(), day.atTime(LocalTime.MAX), AppointmentStatus.SCHEDULED.name());

        return dayAppointments.stream().noneMatch(existing -> overlaps(
                appointmentDateTime, proposedEnd, existing.getAppointmentDate(), existing.getAppointmentDate().plusMinutes(existing.getDurationMinutes())));
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}
