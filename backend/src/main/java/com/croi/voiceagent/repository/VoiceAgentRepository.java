package com.croi.voiceagent.repository;

import com.croi.voiceagent.entity.VoiceAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoiceAgentRepository extends JpaRepository<VoiceAgent, UUID> {

    List<VoiceAgent> findByOrganizationId(UUID organizationId);

    boolean existsByPhoneNumber(String phoneNumber);
}
