package com.croi.appointment.repository;

import com.croi.appointment.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueueStatusRepository extends JpaRepository<QueueStatus, UUID> {

    Optional<QueueStatus> findByOrganizationId(UUID organizationId);
}
