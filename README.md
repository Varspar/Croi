# Croi

AI customer-support platform: Next.js frontend, Spring Boot backend, Postgres + pgvector for
retrieval, Ollama (nomic-embed-text) for embeddings, OpenRouter for chat completions.

## Quick Start with Docker Compose

```bash
# 1. Copy the env template and fill in your OpenRouter key
cp .env.example .env
# edit .env and set OPENROUTER_API_KEY

# 2. Start all services (first run builds images, ~3-5 min)
docker-compose up -d

# 3. Wait for the backend to report ready
docker-compose logs -f croi-backend
# look for: "Started CroiApplication in X seconds"

# 4. Pull the Ollama embedding model (one-time; the container starts empty)
docker exec croi-ollama ollama pull nomic-embed-text

# 5. Services are ready:
# - Frontend:  http://localhost:3000
# - Backend:   http://localhost:8080
# - Ollama:    http://localhost:11434
# - Postgres:  localhost:5432

# 6. Stop everything
docker-compose down
```

Flyway runs all database migrations automatically on backend startup — no manual
migration step is required.

### Notes

- The backend healthcheck hits `/v3/api-docs` (springdoc), since no `/actuator/health`
  endpoint is wired up yet.
- If you change backend or frontend source after the images are built, re-run
  `docker-compose up -d --build` to pick up the changes (there are no bind mounts —
  these are production-style images, not hot-reload dev containers).
- `docker-compose.dev.yml` is a lighter-weight file that starts only Ollama, for local
  development against a backend run outside Docker (e.g. from your IDE).
