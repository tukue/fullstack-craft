# AI Coaching Platform — Generic Implementation Design

Status: draft for agreement | Date: 2026-10-14 | Owner: fullstack-craft monorepo
Stack: React 19 + Vite + TS (Node 24) + Spring Boot 3.5 (Java 25) + `AiProvider` facade + `packages/api-contracts/openapi.yaml`

> Design principle: **business requirement may change**. Core platform is domain-agnostic. Health, Learning, Fitness, Career are plug-in `Domain Packs`, not forks.

## 1. Goal & MVP Definition

Build a generic coaching loop that works for any domain:

```text
Onboarding -> Select Domain -> Set Goal -> Log Check-In -> AI Plan -> Dashboard -> Coach Chat
```

MVP proves genericity with **two domains**: `health` + `learning` sharing the same tables, APIs, and UI shell. Adding a third domain = config + prompts only, no schema or endpoint change.

Out of scope for MVP: wearable auto-sync, meal DB, social, native apps, diagnosis/treatment, multilingual, per-domain native apps.

## 2. Baseline (reuse)

- Frontend: `apps/frontend/src/lib/api.ts` (`VITE_API_URL`), `src/features/ai/AiChat.tsx + useAiChat.ts` (`POST /api/ai/chat`), `src/routes/`.
- Backend: `.../ai/AiProvider.java {name(), model(), chat()}`, `OpenAiCompatible/Anthropic/Gemini/Mistral/Ollama/LocalModel/EchoProvider`, `AiService`, `AiController`, `.../health/HealthController`.
- Contract: `packages/api-contracts/openapi.yaml` is source of truth.
- Infra: `docker-compose.yml`, `AI_PROVIDER`, `AI_OPENAI_*`.

Carried rules: frontend never holds keys; new vendor = new `AiProvider`; contract-first yaml -> DTOs -> TS types.

## 3. Generic Product Design

Core concepts (same for all domains):

| Concept | Generic meaning | Health example | Learning example |
|---|---|---|---|
| Domain | coaching vertical | `health` | `learning` |
| Goal | user target in a domain | sleep 8h, walk 8k steps | finish React + Spring |
| Metric | trackable field defined by domain | sleep_hours, steps, mood 1-5 | study_minutes, focus 1-5 |
| CheckIn | dated metric values + note | daily log | study log |
| Plan | AI output JSON + prompt_version | 3 habits + tips | 3 tasks + resources |
| SafetyPolicy | per-domain guardrails | medical disclaimer + red-flags | minimal |

User journeys (domain-independent):
1. `/onboarding` -> profile + consent -> `POST /api/profiles`
2. `/domains` -> pick domain -> `/goals/new?domain=health` (form rendered from domain definition) -> `POST /api/goals`
3. `/check-in?goalId=` (dynamic fields from domain) -> `POST /api/check-ins`
4. `/dashboard`, `/plan`, `/coach` — same components, copy + fields swap by domain.

## 4. Domain Pack Mechanism (how we stay generic)

No `GoalType.SLEEP` enum in code. Instead a versioned `DomainPack` loaded from `services/backend/src/main/resources/domains/*.yaml`:

```yaml
# domains/health.yaml
id: health
version: v1
goals: [SLEEP, ACTIVITY, NUTRITION, STRESS]
metrics:
  - {key: sleep_hours, type: number, min: 0, max: 24}
  - {key: steps, type: integer, min: 0}
  - {key: mood, type: scale, min: 1, max: 5}
  - {key: adherence, type: scale, min: 1, max: 5}
prompt_pack: health-v1
safety_policy: health-strict
disclaimer: "General wellness info only, not medical advice."
```

```yaml
# domains/learning.yaml
id: learning
version: v1
goals: [FRONTEND, BACKEND, AI_BASICS]
metrics:
  - {key: study_minutes, type: integer, min: 0}
  - {key: focus, type: scale, min: 1, max: 5}
prompt_pack: learning-v1
safety_policy: standard
disclaimer: "Educational suggestions only."
```

Backend: `DomainRegistry` loads packs at startup, validates `domain_id` on goal creation. Frontend: `GET /api/domains` drives dynamic forms — no hardcoded health fields in `CheckInForm`.

Adding/changing business requirement = add/edit one yaml + prompt pack, no migration.

## 5. Data Design (stable across domains)

```text
UserProfile 1 — * Goal 1 — * CheckIn
Goal 1 — * CoachingPlan
DomainPack (config, not table) validates Goal/CheckIn
```

| Entity | Generic fields |
|---|---|
| UserProfile | id, user_id, attributes JSONB (age_range, level, ...), consent_accepted_at |
| Goal | id, profile_id, domain_id, goal_type (string, validated against pack), target_text, target_value JSONB, deadline, status |
| CheckIn | id, goal_id, date, metric_values JSONB, note VARCHAR(500), UNIQUE(goal_id,date) |
| CoachingPlan | id, goal_id, domain_id, period_start, input_snapshot_hash, plan_json JSONB, model, prompt_version, safety_flags |

Postgres + Flyway `V2__coaching_platform.sql` with JSONB, no per-domain columns. Indexes on `(goal_id, date DESC)`, `(goal_id, period_start DESC)`. `DELETE /api/profiles/me` cascades. Never log `note`/`metric_values` raw.

## 6. API Design (generic, extend openapi.yaml)

```text
GET  /api/domains                 -> list packs (drives UI)
POST /api/profiles, GET /api/profiles/me, DELETE /api/profiles/me
POST /api/goals {domain_id, goal_type, target...}, GET /api/goals?domain_id=
POST /api/check-ins {goal_id, metric_values, note}, GET /api/check-ins?goalId=&days=7
POST /api/coaching/plan {goalId} -> {plan, model, prompt_version, safety_flags}
GET  /api/coaching/history?goalId=
# unchanged: /api/health, /api/ai/chat, /api/ai/health
```

Validation: `domain_id + goal_type` checked against `DomainRegistry`; `metric_values` keys/ranges checked against pack; RFC7807 errors. Packages: `.../domain/`, `.../profile/`, `.../goal/`, `.../checkin/`, `.../coaching/`, `.../common/`.

Frontend: `features/domains/`, `features/goals/GoalWizard.tsx (dynamic)`, `features/checkin/CheckInForm.tsx (dynamic)`, `features/coaching/PlanView.tsx (generic renderer)`.

## 7. AI Design (generic orchestration + per-domain prompts)

`AiProvider` unchanged. New:

```text
[Goal + domain pack + last N CheckIns + profile attributes]
 -> CoachingPromptBuilder (selects prompt_pack by domain_id + version)
 -> SafetyFilter.pre (selects policy by domain)
 -> AiProvider.chat()
 -> PlanValidator (generic JSON envelope, per-domain schema)
 -> SafetyFilter.post -> persist CoachingPlan
```

- Prompt packs versioned: `health-v1`, `learning-v1` in `resources/prompts/`. Stored per plan as `prompt_version` for regression.
- Generic output envelope: `{focus, actions[3], tips[3], flags, refer_to_professional}` — domain copy differs, shape same so `PlanView` never forks.
- Fallback: parse fail -> deterministic template per domain + `safety_flags=["fallback_template"]`.
- Policies: `health-strict` (medical red-flag list: chest pain, fainting, ED, self-harm -> `refer_to_professional=true`), `standard` (spam/PII filter only).
- Config: `COACH_MODEL`, `COACH_PROMPT_VERSION`, `AI_PROVIDER=echo` returns canned JSON per domain for local dev.
- Eval: `CoachingPromptTest` parameterized by domain (health normal/red-flag, learning normal/sparse) in `src/test/resources/coaching/{health,learning}/`.
- Phase 2: SSE streaming, `services/ai-python/` + pgvector RAG behind same `/api/coaching/*` facade.

## 8. NFRs: Security, Safety, Compliance

- JWT (Spring Security), `user_id` scoping; demo seed locally.
- Consent gate: `POST /api/coaching/plan` -> 403 if `consent_accepted_at` null. Per-domain disclaimer rendered from pack.
- Posture: ship as wellness/education info; HIPAA path kept open (encryption, audit, export/delete) without hardcoding health assumptions.
- Targets: plan p95 < 8s, CRUD < 300ms, JaCoCo > 70% on new code, Lighthouse > 90.

## 9. Build Plan

1. Platform CRUD + `GET /api/domains` + dynamic forms (no AI), `V2__` migration.
2. Coaching stub via `EchoProvider` (both domains) + generic `PlanView` + golden tests.
3. Real LLM + per-domain safety + history.
4. JWT + rate-limit + charts + `openapi-typescript` + CI + ADR.

Each slice updates yaml first, then DTOs, then TS types.

## 10. Open Decisions

1. Confirm first two domains: `health + learning`?
2. Dynamic-form depth: JSON-schema rendering in MVP vs two hardcoded forms behind shared hook?
3. Safety default: `standard` for all + `health-strict` override (recommended)?

Next: ADR 0002 + `openapi.yaml` extension with generic schemas + `domains/health.yaml`, `domains/learning.yaml` samples.
