# AI Powered Placement Management System

## Dev commands

```bash
# Backend (Java 21, Spring Boot 3.5, Maven wrapper)
cd backend
./mvnw compile                              # Compile only
./mvnw test -Dtest=AuthControllerTest       # Run single test
./mvnw test                                 # All tests
./mvnw spring-boot:run                      # Dev server (needs postgres on :5432)
./mvnw package -DskipTests                  # Build JAR

# Frontend (React 19, Vite 8, TypeScript 6, Tailwind 4)
cd frontend
npm install                                 # Install deps
npm run dev                                 # Dev server on :5173 (no proxy)
npm run build                               # tsc -b && vite build (required before commit)
npm run lint                                # ESLint 10

# Docker (PostgreSQL 16 + backend)
docker compose up -d                        # Start all
docker compose up -d postgres               # DB only (for local backend dev)
docker compose up -d --build                # Rebuild + start
docker compose down                         # Stop
docker compose exec postgres psql -U postgres -d placement_db  # Query DB
```

## Architecture

- **Backend**: `backend/src/main/java/.../Placement_management_system/` — domain packages: `auth/`, `user/`, `drive/`, `jobpost/`, `application/`, `notification/`, `resume/`, `common/`, `config/`
- **Frontend**: `frontend/src/` — pages, hooks, store (Zustand), api (Axios), types, components
- **API**: `http://localhost:8080/api/v1/` — all endpoints under `/api/v1/`
- **Roles**: `ADMIN > PO > RECRUITER > STUDENT` (Spring Security role hierarchy, string-based, no enum)

## Key conventions

- **Hibernate DDL**: `validate` — Flyway owns the schema. Never change DDL directly; write a new migration.
- **API response wrapper**: All endpoints return `ApiResponse<T>` with `{ status, message, data, timestamp }` — see `common/dto/ApiResponse.java`.
- **Register creates profiles**: `AuthService.register()` auto-creates `StudentProfile`/`RecruiterProfile`/`PlacementOfficerProfile` with placeholder values (`"Pending"`) for NOT NULL columns. Profile update endpoint is future work.
- **Registration roles**: accepted values are `STUDENT`, `PO`, `RECRUITER`, `ADMIN` (ADMIN not exposed in frontend). Frontend registration omits ADMIN from role dropdown.
- **JWT**: HS384, 24h expiry, `Authorization: Bearer <token>`. Key from `JWT_SECRET` env var (fallback `dev-secret-key-min-256-bits-long-for-hs256-algorithm`). Max 5MB PDF uploads to `./uploads/`.
- **Auth errors**: Missing/invalid JWT → 401 (via `AuthenticationEntryPoint`). Authenticated but unauthorized → 403 (via `AccessDeniedException` handler).
- **Frontend auth**: Zustand store persisted to localStorage key `auth-storage`. Axios interceptor reads token from store, redirects to `/login` on 401.

## Database

- PostgreSQL 16, database `placement_db`, user/pass `postgres/postgres`
- 10 Flyway migrations (`V1`–`V10`) — see `backend/src/main/resources/db/migration/`
- Admin seed: `admin@placement.com` (POPULATED in V9)

## Testing

- **Backend**: JUnit 5 + `@SpringBootTest(webEnvironment = RANDOM_PORT)` — requires PostgreSQL running. No test profile/application-test.yml exists.
- **Frontend**: No test runner configured.

## Current state

Phase 0/1 complete: auth (register/login/JWT/me), Docker, schema, frontend shell. Core features (drives, job posts, applications, notifications, dashboards) are **not built** — entity classes exist but controllers, services, repositories are missing. Frontend pages for these features are placeholders.
