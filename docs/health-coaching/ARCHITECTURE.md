# Coaching Platform — System Architecture (Generic)

Status: draft | Date: 2026-10-14
Companion to: `docs/health-coaching/DESIGN.md` (generic platform design)
Baseline: `docs/ARCHITECTURE.md` (two-runtime monorepo)

> Health is a domain plug-in, not the architecture. Same containers serve any `DomainPack`.

## 1. Context & Goals

Evolve `fullstack-craft` into a domain-agnostic AI coaching platform. Business can pivot (health -> learning -> career) via config, not rewrite.

Invariants: frontend never holds keys; contract-first `openapi.yaml`; vendor-agnostic `AiProvider`; `AI_PROVIDER=echo|openai|anthropic|gemini|mistral|ollama`.

## 2. Containers

```text
[React SPA] --REST--> [Spring Boot] --HTTPS--> [LLM vendor]
    |                        |
    +-> VITE_API_URL         v
                      [Postgres 16 + pgvector + Flyway V2__coaching_platform]
```

`apps/frontend` calls `/api/*` only. `services/backend` owns domain validation + AI orchestration. `packages/api-contracts` is truth. `docker-compose`: frontend, backend, db.

## 3. Backend Components

Reuse: `ai/AiProvider.java`, `AiProviderRegistry`, all vendor providers, `EchoProvider`, `AiService`, `AiController (POST /api/ai/chat)`, `health/HealthController`.

New (generic):

```text
.../domain/          DomainRegistry, DomainPack, DomainController (GET /api/domains)
.../profile/         ProfileController/Service/Entity/Repository
.../goal/            GoalController/Service (validates domain_id+goal_type via registry)
.../checkin/         CheckInController/Service (validates metric_values via registry)
.../coaching/        CoachingController, CoachingService, CoachingPromptBuilder (pack select),
                     SafetyFilter (policy select), PlanValidator (envelope + per-domain schema)
.../common/          ApiExceptionHandler, SecurityConfig (JWT)
```

Rules: Controller -> Service -> Repository + `AiProvider`. Only `CoachingService` calls LLM for plans. Resources: `resources/domains/*.yaml`, `resources/prompts/{health-v1,learning-v1}.md`, `resources/safety/*.yaml`.

Strategy pattern mirrors `AiProvider`: new domain = new yaml + prompt file, no Java change. New behavior = new `DomainHandler`/`SafetyPolicy` implementation.

## 4. Data (stable schema)

```text
user_profile(id, user_id UNIQUE, attributes JSONB, consent_accepted_at)
goal(id, profile_id FK, domain_id TEXT, goal_type TEXT, target_text, target_value JSONB, deadline, status)
check_in(id, goal_id FK, date, metric_values JSONB, note VARCHAR(500), UNIQUE(goal_id,date))
coaching_plan(id, goal_id FK, domain_id, period_start, input_snapshot_hash, plan_json JSONB, model, prompt_version, safety_flags[])
```

No per-domain columns. Pack defines allowed `goal_type` + `metrics`. Indexes `(goal_id, date DESC)`, `(goal_id, period_start DESC)`. Delete-me cascades. Phase 2: pgvector `embedding` on knowledge library only.

## 5. API (generic)

```text
GET  /api/domains
POST /api/profiles, GET /api/profiles/me, DELETE /api/profiles/me
POST /api/goals, GET /api/goals?domain_id=
POST /api/check-ins, GET /api/check-ins?goalId=&days=7
POST /api/coaching/plan {goalId} -> {plan, model, prompt_version, safety_flags}
GET  /api/coaching/history?goalId=
```

Jakarta Validation + RFC7807. TS types via `openapi-typescript`. Generic envelope lets FE render any domain.

## 6. AI Flow

```text
[Goal + pack + N CheckIns] -> PromptBuilder(pack) -> Safety.pre(policy) -> AiProvider.chat()
 -> PlanValidator(envelope) -> Safety.post -> save CoachingPlan
```

`prompt_version` per row (e.g. `health-v1`, `learning-v1`). Weekly batch; chat injects summary. `input_snapshot_hash` dedupes. `echo` returns per-domain canned JSON locally. Eval parameterized by domain. Phase 2: SSE + `services/ai-python/` RAG behind same facade.

Sequence: `FE POST /api/coaching/plan -> load goal+pack+checkins -> prompt -> Safety.pre -> LLM -> validate -> Safety.post -> save -> render generic PlanView + pack disclaimer`.

## 7. Frontend

```text
lib/api.ts + routes/ + features/domains/useDomains.ts
features/goals/GoalWizard.tsx (renders pack.goals + fields)
features/checkin/CheckInForm.tsx (renders pack.metrics)
features/coaching/PlanView.tsx (renders envelope {focus, actions, tips, flags})
features/ai/AiChat.tsx (reuse + pack disclaimer footer)
```

Server state in TanStack Query only. Recharts for streaks. No keys/prompts in FE.

## 8. Security & Deploy

JWT + `user_id` scoping, consent gate (403 if null), per-domain disclaimer from pack, rate-limit plan 1/day, secrets via env, TLS + encrypted DB, notes scrubbed from logs.

`docker-compose`: frontend:5173, backend:8080, db:5432. Env: `AI_PROVIDER, AI_OPENAI_*, COACH_MODEL, COACH_PROMPT_VERSION, SPRING_DATASOURCE_URL, JWT_SECRET`. Local `echo` + seed; prod `openai` + managed PG. CI: `npm run build:frontend && mvn verify`.

## 9. Change Scenarios

- New domain (career): add `domains/career.yaml` + `prompts/career-v1.md` — no migration, no endpoint, FE auto-renders.
- Change health metrics: bump `health.yaml version` — old plans keep `prompt_version` for audit.
- Swap LLM vendor: `AI_PROVIDER` env only.
