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
@Table(name = "queue_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatus {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "workspace_id", nullable = false, unique = true)
    private UUID organizationId;

    @Column(name = "current_patient_count", nullable = false)
    private Integer currentPatientCount;

    @Column(name = "estimated_wait_minutes", nullable = false)
    private Integer estimatedWaitMinutes;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        this.lastUpdated = LocalDateTime.now();
        if (this.status == null) this.status = "normal";
        if (this.currentPatientCount == null) this.currentPatientCount = 0;
        if (this.estimatedWaitMinutes == null) this.estimatedWaitMinutes = 0;
    }
}
