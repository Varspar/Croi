package com.croi.appointment.repository;

import com.croi.appointment.entity.AvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, UUID> {

    Optional<AvailabilityRule> findByOrganizationIdAndDayOfWeek(UUID organizationId, Integer dayOfWeek);

    List<AvailabilityRule> findByOrganizationId(UUID organizationId);

    void deleteByOrganizationId(UUID organizationId);
}
