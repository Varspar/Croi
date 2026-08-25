package com.croi.voiceagent.repository;

import com.croi.voiceagent.entity.VoiceAgentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoiceAgentConfigRepository extends JpaRepository<VoiceAgentConfig, UUID> {

    Optional<VoiceAgentConfig> findByAgentId(UUID agentId);
}
