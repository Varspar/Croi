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
@Table(name = "call_recordings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallRecording {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID organizationId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "patient_phone", nullable = false)
    private String patientPhone;

    @Column(name = "call_start_time", nullable = false)
    private LocalDateTime callStartTime;

    @Column(name = "call_end_time")
    private LocalDateTime callEndTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /** Path/URL into external audio storage — the file itself is never stored in this row. */
    @Column(name = "audio_file_path", nullable = false)
    private String audioFilePath;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "transcription_status", nullable = false)
    private String transcriptionStatus;

    @Column(name = "storage_size_bytes")
    private Long storageSizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.transcriptionStatus == null) {
            this.transcriptionStatus = "pending";
        }
    }
}
