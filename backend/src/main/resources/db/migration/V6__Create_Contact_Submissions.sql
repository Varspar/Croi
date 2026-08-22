-- ============================================================================
-- V6__Create_Contact_Submissions.sql
-- Purpose: Stores submissions from the public landing page contact form.
-- This endpoint is unauthenticated (anonymous visitors), so no user_id link.
-- ============================================================================

CREATE TABLE contact_submissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    company_name    VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    message         TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, REPLIED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_contact_submissions_email ON contact_submissions (email);
CREATE INDEX idx_contact_submissions_created_at ON contact_submissions (created_at);
CREATE INDEX idx_contact_submissions_status ON contact_submissions (status);
