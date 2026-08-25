package com.croi.appointment.entity;

/** `call_recordings.outcome` is plain VARCHAR(50) — this is for compile-time safety at call sites. */
public enum CallOutcome {
    APPOINTMENT_BOOKED,
    INFORMATION_PROVIDED,
    TRANSFERRED,
    NO_ANSWER,
    OTHER
}
