# AI Powered Placement Management System

## Dev commands

```bash
# Backend (Java 21, Spring Boot 3.5, Maven wrapper)
cd backend
./mvnw compile                              # Compile only
./mvnw test -Dtest=AuthControllerTest       # Run single test
./mvnw test                                 # All tests (needs Docker for Testcontainers)
./mvnw spring-boot:run                      # Dev server (needs postgres on :5432 or docker compose up -d postgres)
./mvnw package -DskipTests                  # Build JAR

# Frontend (React 19, Vite 8, TypeScript 6, Tailwind 4)
cd frontend
npm install                                 # Install deps
npm run dev                                 # Dev server on :5173 (no proxy)
npm run build                               # tsc -b && vite build (required before commit)
npm run lint                                # ESLint 10
npm test                                    # Vitest run
npm run test:watch                          # Vitest watch mode

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
  - Because of hierarchy, `@PreAuthorize("hasRole('STUDENT')")` matches ALL roles (higher roles inherit lower). For exact role checks, manually verify `ROLE_STUDENT` in the GrantedAuthority set instead.

## Key conventions

- **Hibernate DDL**: `validate` — Flyway owns the schema. Never change DDL directly; write a new migration.
- **API response wrapper**: All endpoints return `ApiResponse<T>` with `{ status, message, data, timestamp }` — see `common/dto/ApiResponse.java`.
- **Pagination**: All list endpoints return `ApiResponse<PagedResponse<T>>` with `{ content: T[], page, size, totalElements, totalPages, last }`. Single-item endpoints return `ApiResponse<T>`. See `common/dto/PagedResponse.java`.
- **Register creates profiles**: `AuthService.register()` auto-creates `StudentProfile`/`RecruiterProfile`/`PlacementOfficerProfile` with placeholder values (`"Pending"`) for NOT NULL columns. Profile update endpoint is future work.
- **Registration roles**: accepted values are `STUDENT`, `PO`, `RECRUITER`, `ADMIN` (ADMIN not exposed in frontend). Frontend registration omits ADMIN from role dropdown.
- **JWT**: HS384, 24h expiry, `Authorization: Bearer <token>`. Key from `JWT_SECRET` env var (fallback `dev-secret-key-min-256-bits-long-for-hs256-algorithm`). Max 5MB PDF uploads to `./uploads/`.
- **Auth errors**: Missing/invalid JWT → 401 (via `AuthenticationEntryPoint`). Authenticated but unauthorized → 403 (via `AccessDeniedException` handler).
- **Resume download**: Only the owning STUDENT can download their own resume. Uses exact role check (not `@PreAuthorize`) to bypass Spring Security role hierarchy. Non-students get 403; wrong-owner gets 403.
- **Frontend auth**: Zustand store persisted to localStorage key `auth-storage`. Axios interceptor reads token from store, redirects to `/login` on 401.
- **Frontend pagination**: All list pages use `Pagination` component (`components/shared/Pagination.tsx`) with `page` state and `onPageChange` callback. Always reset page to 0 after create/delete operations via mutation `onSuccess`.

## Database

- PostgreSQL 16, database `placement_db`, user/pass `postgres/postgres`
- 13 Flyway migrations (`V1`–`V13`) — see `backend/src/main/resources/db/migration/`
- Admin seed: `admin@placement.com` (POPULATED in V9)
- Indexes: V10 adds common query indexes; V12 adds profile/user FK indexes; V13 adds full non-partial index on `notifications(user_id)` (V10's partial index `WHERE is_read = false` only serves the unread-count query).

## Testing

- **Backend**: JUnit 5 + Testcontainers (PostgreSQL 16-alpine). All integration tests extend `AbstractIntegrationTest` in `common/` package, which starts a singleton `PostgreSQLContainer` and provides `@DynamicPropertySource` for datasource config. Requires Docker running on the host.
  - Running all tests: `./mvnw test` (takes ~10s after container is running)
  - Single test: `./mvnw test -Dtest=AuthControllerTest`
  - Test pattern: each test class uses `TestRestTemplate` for HTTP calls, `ObjectMapper` for JSON assertions. Helper methods (`registerUser`, `jsonRequest`) inherited from base class.
  - No test database cleanup between classes — each test generates unique emails. Asserts use `>=` not exact counts.
- **Frontend**: Vitest + jsdom + React Testing Library. Test files in `src/test/`. Vitest config in `vitest.config.ts` (separate from `vite.config.ts` to avoid TypeScript version conflict). Config: `npm test` (vitest run), `npm run test:watch` (vitest watch).

## Current state

Phases 0–2 complete. All core CRUD endpoints exist and are paginated:
- **Auth**: register, login, getMe, JWT filter
- **Drives**: create/read/update/delete/status by PO, list+get by all authenticated
- **Job posts**: create/read/update/delete/status by RECRUITER, list by drive, list my posts
- **Applications**: create by STUDENT, list my, list by job post (RECRUITER), list by drive (PO), status update (RECRUITER/PO)
- **Notifications**: auto-created on application submit/status change, list, unread count, mark-read, mark-all-read
- **Resumes**: upload/download by owning STUDENT only, PDF-only 5MB limit, stored to `./uploads/`

What is NOT built (Phase 3+):
- Profile update endpoints (StudentProfile, RecruiterProfile, PlacementOfficerProfile placeholders are write-once)
- Admin endpoints (user management, system stats)
- Application withdrawal (STUDENT)
- Resume download for RECRUITER/PO (currently STUDENT-only)
- Auto-close job post when vacancies filled
- WebSocket/SSE push for real-time notifications
- AI scoring (`aiScore` column exists on Application, no integration)
- Analytics, audit logging, rate limiting, CI/CD
