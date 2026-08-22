package com.croi.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guardrail_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardrailLog {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID organizationId;

    @Column(name = "detected_request", columnDefinition = "TEXT", nullable = false)
    private String detectedRequest;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "was_out_of_scope")
    private Boolean wasOutOfScope;

    @Column(name = "required_alternative")
    private String requiredAlternative;

    @Column(name = "is_emergency")
    private Boolean isEmergency;

    @Column(name = "patient_phone")
    private String patientPhone;

    @Column(name = "call_recording_id")
    private UUID callRecordingId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
