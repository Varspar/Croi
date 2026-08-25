package com.croi.voiceagent.entity;

import com.croi.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "voice_agent_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VoiceAgentConfig extends BaseEntity {

    @Column(name = "agent_id", nullable = false, unique = true)
    private UUID agentId;

    @Column(name = "system_prompt", columnDefinition = "TEXT", nullable = false)
    private String systemPrompt;

    @Column(nullable = false)
    private String tone;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private String model;

    @Column(name = "max_duration_seconds", nullable = false)
    private Integer maxDurationSeconds;
}
