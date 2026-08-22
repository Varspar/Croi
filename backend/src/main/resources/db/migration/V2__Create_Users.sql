-- ============================================================================
-- V2__Create_Users.sql
-- Purpose: Stores platform user accounts (login credentials + basic profile).
-- Users belong to organizations via the organization_members table (see V3).
-- ============================================================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    avatar_url      VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Fast lookup for login and uniqueness checks
CREATE INDEX idx_users_email ON users (email);

-- Support filtering active/inactive accounts
CREATE INDEX idx_users_is_active ON users (is_active);
