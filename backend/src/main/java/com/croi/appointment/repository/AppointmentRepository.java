package com.croi.appointment.repository;

import com.croi.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByOrganizationIdAndAppointmentDateBetween(UUID organizationId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByOrganizationIdAndAppointmentDateBetweenAndStatus(UUID organizationId, LocalDateTime start, LocalDateTime end, String status);

    List<Appointment> findByOrganizationIdAndPatientPhone(UUID organizationId, String patientPhone);

    List<Appointment> findByOrganizationIdAndStatusOrderByAppointmentDateAsc(UUID organizationId, String status);
}
