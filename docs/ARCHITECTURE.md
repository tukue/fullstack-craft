# Architecture

Generic monorepo for fullstack + AI. Two runtimes only: Node 24 (frontend) + Java 25 (backend).

```
apps/frontend/            React 19 + Vite + TS + TanStack Query
  src/features/ai/        AI UI only — no vendor SDKs, calls /api/ai/*
  src/lib/api.ts          Single fetch wrapper (VITE_API_URL)
  src/routes/             Add react-router routes here
services/backend/         Java 25 + Spring Boot 3.5 + Maven
  .../health/             /api/health
  .../ai/                 Generic AI integration
    AiProvider.java       Interface: name(), model(), chat()
    OpenAiCompatibleProvider.java  OpenAI/Azure/Groq/Together/Ollama via base-url swap
    EchoProvider.java     Dev stub (AI_PROVIDER=echo)
    AiService.java        Business logic, returns provider+model for observability
    AiController.java     POST /api/ai/chat, GET /api/ai/health
packages/api-contracts/   openapi.yaml — single source of truth
infra/docker/             reserved for k8s/terraform later
docs/adr/                 architecture decision records
```

## Rules (senior defaults)

1. Frontend never holds AI keys. All AI traffic goes through backend.
2. New AI vendor = new `AiProvider` implementation + env config, no controller change.
3. Contract-first: update `openapi.yaml`, then backend DTOs, then frontend types.
4. `AI_PROVIDER` selects implementation: `echo` (local), `openai` (any OpenAI-compatible endpoint).
5. For Python RAG/embeddings later, add `services/ai-python/` behind the same `/api/ai/*` facade — frontend stays unchanged.
