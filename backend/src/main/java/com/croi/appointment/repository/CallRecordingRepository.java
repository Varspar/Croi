package com.croi.appointment.repository;

import com.croi.appointment.entity.CallRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CallRecordingRepository extends JpaRepository<CallRecording, UUID> {

    List<CallRecording> findByOrganizationIdOrderByCallStartTimeDesc(UUID organizationId);

    List<CallRecording> findByAppointmentId(UUID appointmentId);
}
