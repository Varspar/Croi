#!/usr/bin/env bash
# Drops and recreates the local Croi database so Flyway rebuilds it from scratch
# on the next application startup (V1__Initial_Schema.sql ... V5__Create_Knowledge_Base.sql).
#
# Usage: ./reset-db.sh [db_name] [db_user] [db_host] [db_port]
# Defaults match the local dev settings in application.yml.

set -euo pipefail

DB_NAME="${1:-croi}"
DB_USER="${2:-postgres}"
DB_HOST="${3:-localhost}"
DB_PORT="${4:-5432}"

export PGPASSWORD="${DB_PASSWORD:-postgres}"

echo "Dropping database '${DB_NAME}' on ${DB_HOST}:${DB_PORT} (if it exists)..."
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d postgres \
  -c "DROP DATABASE IF EXISTS ${DB_NAME};"

echo "Creating database '${DB_NAME}'..."
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d postgres \
  -c "CREATE DATABASE ${DB_NAME};"

echo "Done. Flyway will recreate the schema on the next application startup."
