-- ============================================================================
-- V3__Create_Organizations.sql
-- Purpose: Organizations represent the businesses using Croi. Each org can
-- have multiple users (members) with different roles. Conversations,
-- documents, and AI configuration are all scoped to an organization.
-- ============================================================================

CREATE TABLE organizations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    owner_id    UUID NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_organizations_slug ON organizations (slug);
CREATE INDEX idx_organizations_owner_id ON organizations (owner_id);

-- Join table linking users to organizations with a role.
-- A user can belong to multiple organizations; membership is unique per pair.
CREATE TABLE organization_members (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role            VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_organization_members_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_org_members_organization_id ON organization_members (organization_id);
CREATE INDEX idx_org_members_user_id ON organization_members (user_id);
