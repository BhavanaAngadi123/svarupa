# Svarupa — AI Dance-Learning Platform

**Meet your true form.** Your teacher sets the destination — the perfect form. Svarupa watches you practice and reroutes you back to it, joint by joint, rep by rep.

This is the full-stack Java platform behind the [Svarupa working prototype](https://bhavanaangadi123.github.io/svarupa-prototype/) (a static client-side demo — untouched by this repo). Real-time pose tracking stays in the browser (MediaPipe); this backend is the platform: accounts, the teacher reference library, scored practice sessions, progress analytics, an AI practice coach, and premium subscriptions.

## Stack

- **Java 25**, **Spring Boot 4.1.x**, **Vaadin 25** (dark luminous UI)
- **Spring Security + JWT** (student / teacher / admin roles, refresh rotation)
- **Spring Data JPA + PostgreSQL + Flyway 13** (migrations V1–V5)
- **Spring AI** — AI practice coach via Ollama, with graceful rule-based fallback when Ollama is unavailable (e.g. Render free tier)
- **Stripe** test-mode subscriptions (disabled by default)
- **OpenAPI/Swagger** (`/swagger-ui.html`), **Actuator** (`/actuator/health`)

## How it works

1. **Teachers record reference steps** — eight canonical joint angles (`lKnee, rKnee, lElbow, rElbow, lShoulder, rShoulder, lHip, rHip`, in degrees), the same contract as the client pose tracker.
2. **Students practice live** — the embedded tracker (`/svarupa-tracker/tracker.html`) draws the skeleton, scores each frame against the reference with the prototype's exact formula (8° tolerance band), and posts the session.
3. **The backend scores and stores** per-joint 0–1 scores, overall % match, and corrections; the practice count on the reference bumps.
4. **Progress analytics** — totals, averages, day streaks, trend lines, and per-step improvement for students; teachers see every follower's progress on their dashboard.
5. **AI coach** — when Ollama is reachable, a local model turns joint deviations into warm coaching cues; otherwise deterministic corrections keep every endpoint working.

## Run locally

Prerequisites: Java 25, Maven, PostgreSQL (or Docker).

```bash
cd backend
# 1. Create the database
createdb svarupa
# 2. Set the required secret (>= 32 chars)
export SVARUPA_JWT_SECRET="$(openssl rand -base64 48)"
# 3. Point at your database (defaults shown)
export DATABASE_URL=jdbc:postgresql://localhost:5432/svarupa
export DATABASE_USER=svarupa
export DATABASE_PASSWORD=svarupa
# 4. Run (dev profile skips the Vaadin frontend build)
mvn -Pdev spring-boot:run
```

Open http://localhost:8080 — sign in, or register as a teacher to record references. A demo teacher (`guru@svarupa.app` / `Svarupa!123`) and three Kuchipudi reference steps are seeded on a fresh database.

Optional: run Ollama locally (`ollama serve` + `ollama pull qwen3:8b`) to enable the AI coach. Without it, `/api/coach/status` reports `{"enabled":true,"aiAvailable":false}` and coaching falls back to rule-based corrections.

## Tests

```bash
cd backend
mvn -Pdev test
```

65 tests: scoring engine (mirrors the client formula exactly), auth, references + RBAC, practice sessions, progress analytics, coach fallback, follows + teacher dashboard, payments (disabled mode), HTTP security.

## Deploy to Render (free)

1. Push this repo to GitHub as `BhavanaAngadi123/svarupa`.
2. In [Render](https://dashboard.render.com): **New → Blueprint**, connect the repo.
3. Render reads `render.yaml`, creates the web service + PostgreSQL, and deploys. Done — no keys needed (Stripe stays disabled, coach runs in fallback mode).

Notes:
- Flyway is pinned to 13.x **plus** the `flyway-database-postgresql` module — required for PostgreSQL 18 (Render's current version). Spring Boot 4.1.x manages Flyway 12 otherwise, which cannot run on PG 18.
- `JAVA_OPTS` caps the JVM for the 512 MB free instance (`-Xmx350m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC`).
- The free service sleeps after ~15 min idle; first request wakes it (~30s).

## API overview

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `/api/auth/login`, `/api/auth/refresh` |
| References | `GET /api/references`, `GET /api/references/{id}`, `POST/PUT/DELETE` (teacher/admin) |
| Sessions | `POST /api/sessions`, `GET /api/sessions/mine` |
| Progress | `GET /api/progress/summary`, `GET /api/progress/by-reference` |
| Teachers | `POST/DELETE /api/teachers/{id}/follow`, `GET /api/teachers/mine`, `GET /api/teachers/dashboard` |
| Coach | `GET /api/coach/status`, `POST /api/coach/feedback` |
| Payments | `POST /api/payments/premium/checkout`, `GET /api/payments/mine` |

Full interactive docs at `/swagger-ui.html` when running.
