# AI Powered Placement Management System

## Dev commands

```bash
# Backend (Java 21, Spring Boot 3.5.14, Maven wrapper)
cd backend
./mvnw compile                              # Compile only
./mvnw test -Dtest=AuthControllerTest       # Single test
./mvnw test                                 # All tests (needs Docker for Testcontainers)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run   # Dev server (needs postgres on :5432 or docker compose up -d postgres)
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

- **Backend** (`com.dhairya.Placement_management_system`): packages — `auth/`, `user/`, `admin/`, `drive/`, `jobpost/`, `application/`, `notification/`, `resume/`, `ai/`, `analytics/`, `insights/`, `common/`, `config/`
- **Frontend** (`frontend/src/`): `pages/`, `hooks/` (TanStack React Query), `api/` (Axios), `store/` (Zustand), `types/`, `components/`
- **API**: `http://localhost:8080/api/v1/` — all endpoints under `/api/v1/`
- **Roles**: `ADMIN > PO > RECRUITER > STUDENT` (Spring Security role hierarchy, string-based, no enum)
  - `@PreAuthorize("hasRole('STUDENT')")` matches ALL roles due to hierarchy. For exact role checks, manually verify `ROLE_STUDENT` in the GrantedAuthority set.
- **Auth principal type**: `UUID` — controllers cast `auth.getPrincipal()` to `(UUID)`

## Key conventions

- **Hibernate DDL**: `validate` — Flyway owns the schema. Never change DDL directly; write a new migration.
- **API response wrapper**: All endpoints return `ApiResponse<T>` with `{ status, message, data, timestamp }`. List endpoints return `ApiResponse<PagedResponse<T>>` with `{ content: T[], page, size, totalElements, totalPages, last }` (default page size 20). Frontend must unwrap `.data` from the response.
- **Auth**: Register + login return JWT (HS256, 24h expiry, `Authorization: Bearer <token>`). Key from `JWT_SECRET` env var (no hardcoded fallback; must be set via `.env` or environment). Missing/invalid JWT → 401; authenticated but unauthorized → 403.
- **Register auto-creates profiles**: `AuthService.register()` creates `StudentProfile`/`RecruiterProfile`/`PlacementOfficerProfile` with placeholder values (`"Pending"`) for NOT NULL columns. Profile update endpoints at `PUT /api/v1/profile/{student,recruiter,po}`.
- **Registration roles**: `STUDENT`, `PO`, `RECRUITER`, `ADMIN` (ADMIN not exposed in frontend dropdown).
- **Frontend auth**: Zustand store persisted to localStorage key `auth-storage`. Axios interceptor reads token from store, redirects to `/login` on 401. No Vite proxy — frontend talks directly to backend via `VITE_API_URL` env var (fallback `http://localhost:8080/api/v1`).
- **Frontend stack**: TanStack React Query for data fetching, react-hook-form + zod for forms, react-hot-toast for notifications, react-router-dom v7 for routing, Tailwind CSS v4 (`@tailwindcss/vite` plugin).
- **File uploads**: Max 5MB PDF, stored in `./uploads`. Resume upload: POST `/api/v1/resumes/upload` (STUDENT only). Resume download: GET `/api/v1/applications/{id}/resume` (authenticated users, owner-only access check).
- **Pagination**: Frontend uses `Pagination` component (`components/shared/Pagination.tsx`) with `page` state and `onPageChange` callback. Always reset page to 0 after create/delete via mutation `onSuccess`.
- **Backend**: constructor injection (no `@Autowired` on fields), Lombok on all entities/DTOs (`@Getter @Setter`).
- **Frontend TS strict**: `noUnusedLocals`, `noUnusedParameters`, `verbatimModuleSyntax` (use `import type`), `erasableSyntaxOnly` (no enums/namespaces). Build fails on any of these.

## Database

- PostgreSQL 16, database `placement_db`, user/pass `postgres/postgres`
- 16 Flyway migrations (V1–V16) — see `backend/src/main/resources/db/migration/`
- Admin seed: `admin@placement.com` / `Admin@123` (V9)

## Testing

- **Backend**: JUnit 5 + Testcontainers (PostgreSQL 16-alpine). All integration tests extend `AbstractIntegrationTest` in `common/` package (singleton `PostgreSQLContainer`, `@DynamicPropertySource`). Requires Docker running.
  - Commands: `./mvnw test` (all, ~10s), `./mvnw test -Dtest=AuthControllerTest` (single)
  - Pattern: `TestRestTemplate` for HTTP, `ObjectMapper` for JSON, helper methods (`registerUser`, `jsonRequest`) from base class
  - No test DB cleanup between classes — each test generates unique emails. Asserts use `>=` not exact counts.
- **Frontend**: Vitest 3 + jsdom + React Testing Library. Test files in `src/test/`. Vitest config in separate `vitest.config.ts` (not `vite.config.ts` — avoids TypeScript version conflict). Commands: `npm test` (vitest run), `npm run test:watch` (vitest watch).

## Not yet built

- WebSocket/SSE push for real-time notifications
- Auto-close job post when vacancies filled
- Gemini API scoring (future, async)
- Analytics materialized views (for >100K rows)
- Audit logging, rate limiting, CI/CD
