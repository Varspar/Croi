package com.croi.appointment.entity;

/**
 * The `appointments.status` column is plain VARCHAR(50) (no Postgres-native enum
 * type, matching every other status-like column in this schema), so any string
 * fits. This enum exists purely for compile-time safety at Java call sites —
 * always store/compare via {@link #name()}.
 */
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
