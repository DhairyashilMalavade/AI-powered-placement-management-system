# AI-Powered Placement Management System

A full-stack web application for managing campus placements, featuring AI-driven resume parsing and candidate scoring. Built with Spring Boot 3.5 + React 19.

## Features

- **Role-based access**: ADMIN, Placement Officer (PO), Recruiter, Student — each with tailored dashboards and permissions
- **Drive management**: POs create and manage placement drives with eligibility criteria (GPA, graduation year, skills)
- **Job post management**: Recruiters post jobs under drives, manage vacancies, track fill status
- **Application lifecycle**: Students apply to jobs; applications flow through APPLIED → UNDER_REVIEW → SHORTLISTED → ACCEPTED/REJECTED
- **AI resume parsing**: Apache Tika extracts text and skills from uploaded PDF resumes
- **AI candidate scoring**: Keyword-based scoring matches student skills against job requirements; ranked lists for recruiters
- **Candidate ranking**: Ranked view shows AI scores, rationale, and feedback for each applicant
- **Recruiter insights**: Skill gap analysis, score distribution, and application funnel charts
- **Analytics dashboard**: Overview stats, drive performance, and application pipelines for POs/Admins
- **Notification system**: Auto-generated notifications on application submission and status changes; mark as read
- **Resume management**: PDF upload (max 5 MB), secure per-student storage, snapshot on application
- **Admin panel**: User search/filter, role changes, account activation/deactivation, system statistics
- **Withdrawal**: Students can withdraw applications before terminal status

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Java, Spring Boot | 21, 3.5.14 |
| Auth | JWT (HS384, JJWT 0.12.6), Spring Security | 24h expiry |
| Database | PostgreSQL, Flyway, Hibernate | 16, 16 migrations |
| AI/ML | Apache Tika, keyword scoring | 3.1.0 |
| Frontend | React, TypeScript, Vite | 19, 6.0, 8.0 |
| State | TanStack React Query, Zustand | 5.100, 5.0 |
| Forms | react-hook-form, zod | 7.76, 4.4 |
| CSS | Tailwind CSS | 4.3 |
| Charts | Recharts | 3.8 |
| Testing | JUnit 5, Testcontainers, Vitest, RTL | — |
| Build | Maven Wrapper, npm | — |
| Container | Docker Compose (PostgreSQL 16) | — |

## Architecture

```
┌─────────────┐     HTTP/JSON      ┌──────────────┐     JDBC      ┌────────────┐
│  React SPA  │ ────────────────── │  Spring Boot  │ ──────────── │ PostgreSQL │
│  :5173      │   Authorization:   │  :8080        │              │  :5432     │
│             │   Bearer <JWT>     │  /api/v1/*    │              │            │
└─────────────┘                    └──────────────┘              └────────────┘
       │                                  │
       │                                  ├── JWT Filter (stateless auth)
       │                                  ├── Flyway (schema migrations)
       │                                  ├── Apache Tika (resume parsing)
       │                                  └── File storage (./uploads/)
       │
  TanStack Query ── Axios ── Zustand (auth store persisted to localStorage)
```

All API responses use the `ApiResponse<T>` wrapper:

```json
{ "status": 200, "message": "Success", "data": { ... }, "timestamp": "2026-06-03T12:00:00" }
```

Paginated endpoints return `PagedResponse<T>`:

```json
{ "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3, "last": false }
```

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- Docker (for PostgreSQL)

### 1. Start the database

```bash
docker compose up -d postgres
```

### 2. Start the backend

```bash
cd backend
export JWT_SECRET="your-256-bit-secret"
./mvnw spring-boot:run
```

The backend starts on `http://localhost:8080`. Flyway automatically applies all 16 migrations.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

### 4. Seed admin credentials

| Email | Password |
|-------|----------|
| admin@placement.com | Admin@123 |

## All-in-one with Docker Compose

```bash
docker compose up -d --build
```

This starts PostgreSQL + the backend on port 8080. Frontend still needs `npm run dev` separately.

## Project Structure

### Backend (`backend/`)

```
src/main/java/com/dhairya/Placement_management_system/
├── admin/          AdminController, AdminService, SystemStatsResponse
├── ai/             AIScorer, KeywordScorer, ResumeParser (Tika), ScoringResult
├── analytics/      AnalyticsController, AnalyticsService, funnel/overview/perf DTOs
├── application/    Application(Entity), ApplicationController, ApplicationService, DTOs
├── auth/           AuthController, AuthService, JwtTokenProvider, JwtAuthenticationFilter
├── common/         ApiResponse<T>, PagedResponse<T>, GlobalExceptionHandler, BusinessException
├── config/         SecurityConfig (Spring Security chain), CorsConfig
├── drive/          Drive(Entity), DriveController, DriveService, DTOs
├── insights/       InsightsController, InsightsService, skill-gap/distribution DTOs
├── jobpost/        JobPost(Entity), JobPostController, JobPostService, DTOs
├── notification/   Notification(Entity), NotificationController, NotificationService
├── resume/         ResumeController, ResumeService, FileStorageService (local)
└── user/           User(Entity), 3 Profile entities, ProfileController, ProfileService, DTOs, ParsedResume
```

### Frontend (`frontend/`)

```
src/
├── api/            Axios client + endpoint modules (auth, drives, jobPosts, applications, etc.)
├── components/
│   ├── layout/     AppLayout (sidebar), ProtectedRoute
│   ├── shared/     Spinner, Skeleton, Pagination, SearchInput, EmptyState, ErrorBoundary, StatusBadge
│   ├── drives/     DriveCard, DriveForm, DriveStatusBadge
│   ├── jobposts/   JobPostCard, JobPostForm, JobPostStatusBadge
│   └── applications/ ApplicationStatusBadge
├── hooks/          TanStack React Query hooks (useAuth, useDrives, useJobPosts, etc.)
├── pages/          13 route pages (Login, Register, Dashboard, Drives, DriveDetail, etc.)
├── store/          Zustand auth store (persisted to localStorage)
├── types/          TypeScript interfaces matching backend DTOs
└── test/           Vitest + RTL test files
```

## API Endpoints

### Auth (`/api/v1/auth`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /register | Public | Register (email, password, fullName, role) |
| POST | /login | Public | Login, returns JWT |
| GET | /me | Any | Current user info |

### Drives (`/api/v1/drives`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | / | Any | List drives (search, status, page) |
| GET | /{id} | Any | Drive details |
| POST | / | PO | Create drive |
| PUT | /{id} | PO | Update drive |
| DELETE | /{id} | PO | Delete drive (cascades) |
| PATCH | /{id}/status | PO | Update status (DRAFT/ACTIVE/CLOSED) |

### Job Posts (`/api/v1/job-posts`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | /drive/{driveId} | Any | Posts under a drive |
| GET | /my | Recruiter | My job posts |
| GET | /{id} | Any | Post details |
| POST | / | Recruiter | Create post |
| PUT | /{id} | Recruiter | Update post |
| DELETE | /{id} | Recruiter | Delete post |
| PATCH | /{id}/status | Recruiter | Update status (OPEN/FILLED/CANCELLED) |

### Applications (`/api/v1/applications`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | / | Student | Apply to a job |
| GET | /my | Student | My applications |
| GET | /drive/{driveId} | PO | By drive |
| GET | /job-post/{jobPostId} | Recruiter | By job post |
| GET | /job-post/{jobPostId}/ranked | Any | AI-ranked candidates |
| GET | /{id} | Any | Application detail |
| GET | /{id}/resume | Any | Download resume PDF |
| PATCH | /{id}/withdraw | Student | Withdraw |
| PATCH | /{id}/status | Recruiter/PO | Update status |

### Notifications (`/api/v1/notifications`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | Any | My notifications |
| GET | /unread-count | Any | Unread count |
| PATCH | /{id}/read | Any | Mark read |
| PATCH | /read-all | Any | Mark all read |

### Resumes (`/api/v1/resumes`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | /upload | Student | Upload PDF (max 5 MB) |
| GET | /{filename} | Student | Download own resume |

### Profile (`/api/v1/profile`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | /me | Any | My profile |
| PUT | /student | Student | Update student profile |
| PUT | /recruiter | Recruiter | Update recruiter profile |
| PUT | /po | PO | Update PO profile |

### Admin (`/api/v1/admin`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | /users | ADMIN | List/search users |
| GET | /users/{id} | ADMIN | User details |
| PATCH | /users/{id}/role | ADMIN | Change role |
| PATCH | /users/{id}/active | ADMIN | Toggle active |
| GET | /stats | ADMIN | System stats |

### Analytics (`/api/v1/analytics`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | /overview | ADMIN/PO | Overview stats |
| GET | /drive-performance | ADMIN/PO | Per-drive metrics |
| GET | /application-funnel | ADMIN/PO | Funnel breakdown |

### Insights (`/api/v1/insights`)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | /skill-gaps | RECRUITER/PO/ADMIN | Skill gap analysis |
| GET | /overview | RECRUITER/PO/ADMIN | Score dist + funnel |

## Database

PostgreSQL 16 with 16 Flyway migrations. Key tables:
- `users` — accounts with role (STUDENT/PO/RECRUITER/ADMIN) and active flag
- `student_profiles` / `recruiter_profiles` / `placement_officer_profiles` — per-role profile data
- `drives` — placement drives with eligibility criteria
- `job_posts` — positions under drives, linked to recruiters
- `applications` — student applications with status, AI score, resume snapshot
- `notifications` — user notifications with read state and link
- `parsed_resumes` — extracted text/skills from uploaded PDFs

Query the database:

```bash
docker compose exec postgres psql -U postgres -d placement_db
```

## Testing

### Backend

```bash
cd backend
./mvnw test                    # All tests (needs Docker for Testcontainers)
./mvnw test -Dtest=AuthControllerTest  # Single test
```

Tests use JUnit 5 + Testcontainers (PostgreSQL 16-alpine). All integration tests extend `AbstractIntegrationTest`.

### Frontend

```bash
cd frontend
npm test                       # Vitest run
npm run test:watch             # Vitest watch mode
```

Tests use Vitest + jsdom + React Testing Library.

## AI Scoring

Resume scoring works in two phases:

1. **Parsing**: When a student uploads a PDF resume, Apache Tika extracts raw text. Skills, experience years, and education are parsed and stored in `parsed_resumes`.

2. **Scoring**: When a student applies to a job or on demand, the `KeywordScorer` compares the student's extracted skills against the job post's required skills (from the parent drive). Each match adds points proportional to its position. The result includes a score (0-100), rationale, and actionable feedback.

Current scoring uses keyword matching only. Future versions will integrate Gemini API for semantic scoring.

## Environment Variables

### Backend

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | Yes | — | HS256 key (min 256 bits) |
| `SPRING_DATASOURCE_URL` | No | `jdbc:postgresql://localhost:5432/placement_db` | DB URL |
| `SPRING_DATASOURCE_USERNAME` | No | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | No | `postgres` | DB password |

### Frontend

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `VITE_API_URL` | No | `http://localhost:8080/api/v1` | Backend API base URL |

## Security

- JWT-based stateless authentication (HS384, 24h expiry)
- BCrypt password hashing
- Role-based access with `@PreAuthorize` (ADMIN > PO > RECRUITER > STUDENT hierarchy)
- Secure file access: students can only download their own resume via the `/resumes/{filename}` endpoint
- Cross-application resume access is gated: the `/applications/{id}/resume` endpoint verifies ownership
- Custom 401 JSON response for unauthenticated requests
- CORS restricted to `localhost:5173` and `localhost:5174`

## Not Yet Built

- WebSocket/SSE push notifications
- Auto-close job posts when vacancies filled
- Gemini API semantic scoring
- Analytics materialized views for large-scale data
- Audit logging and rate limiting
- CI/CD pipeline
