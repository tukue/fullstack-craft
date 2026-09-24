# ADR 0001: Two-runtime monorepo with provider-agnostic AI

Date: 2026-09-24
Status: accepted

## Context

Need a generic structure supporting React frontend, Java backend, and AI features
without locking into one AI vendor or adding extra runtimes prematurely.

## Decision

- Frontend: React 19 + Vite + TypeScript on Node 24.
- Backend: Java 25 + Spring Boot on Maven.
- AI: `AiProvider` interface in backend; OpenAI-compatible HTTP provider by default,
  `echo` stub for local dev. Frontend calls backend only.
- Shared contract in `packages/api-contracts/openapi.yaml`.

## Consequences

- Adding Anthropic/Ollama = new provider class + config, no API change.
- Python AI service can be added later behind the same REST facade.
- Requires Node >=24 and Java 25 (see `.nvmrc`, `.java-version`).
