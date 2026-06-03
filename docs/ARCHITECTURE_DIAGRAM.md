# Architecture Diagram Plan

This document describes the architectural diagrams to create for the Placement Management System. Each diagram targets a specific audience and level of detail.

---

## 1. System Context Diagram (C4 Level 1)

**Audience**: Non-technical stakeholders, new team members

**What to show**: The system as a black box interacting with external actors.

### Elements

```
┌──────────────┐     ┌──────────────────────┐     ┌──────────────┐
│   Student    │     │                      │     │  Recruiter   │
│  (Browser)   │────▶│  Placement Management│◀────│  (Browser)   │
│              │     │       System         │     │              │
└──────────────┘     │                      │     └──────────────┘
                     │  (Single-page app +  │
┌──────────────┐     │   Spring Boot API)   │     ┌──────────────┐
│ Placement    │────▶│                      │◀────│   Admin     │
│ Officer      │     └──────────────────────┘     │  (Browser)   │
│  (Browser)   │              │                   └──────────────┘
└──────────────┘              │
                              ▼
                     ┌──────────────────┐
                     │   PostgreSQL 16  │
                     │  (Docker/Remote) │
                     └──────────────────┘
```

**Tool**: Mermaid or Draw.io

---

## 2. Container Diagram (C4 Level 2)

**Audience**: Developers, DevOps engineers

**What to show**: High-level technology choices and communication protocols.

### Containers

| Container | Technology | Port | Description |
|-----------|-----------|------|-------------|
| **React SPA** | React 19 + Vite + TypeScript 6 | 5173 | Client-side rendered single-page application |
| **Spring Boot API** | Java 21 + Spring Boot 3.5 | 8080 | REST API with JWT auth, Flyway migrations, file storage |
| **PostgreSQL** | PostgreSQL 16 | 5432 | Relational database with 15+ tables |
| **File System** | Local disk (`./uploads/`) | — | PDF resume storage |

### Communication

```
┌──────────────┐   HTTP/JSON     ┌──────────────┐   JDBC      ┌──────────┐
│  React SPA   │◀──────────────▶│  Spring Boot  │◀──────────▶│PostgreSQL│
│  :5173       │  Bearer <JWT>  │  :8080        │             │ :5432    │
│              │                │               │             │          │
│ Axios →      │                │ /api/v1/*     │  Flyway     │ 16 migr. │
│ TanStack Q.  │                │               │  (migrate)  │          │
└──────────────┘                └───────┬───────┘             └──────────┘
                                        │
                                  ┌─────┴──────┐
                                  │ ./uploads/  │
                                  │ (PDF files) │
                                  └────────────┘
```

**Tool**: Structurizr (C4 native), Mermaid, or Lucidchart

---

## 3. Backend Component Diagram (C4 Level 3)

**Audience**: Backend developers

**What to show**: Spring Boot packages, their responsibilities, and data flow.

### Layout

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Spring Boot Application                       │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │  Auth   │  │  Drive  │  │ JobPost  │  │Applicat. │  │Notif.  │ │
│  │Controller│  │Controller│  │Controller│  │Controller│  │Controll│ │
│  │ Service │  │ Service │  │ Service  │  │ Service  │  │ Service│ │
│  │ JWT Filter│  │ Repository│  │ Repository│  │ Repos.   │  │ Repos. │ │
│  └────┬────┘  └────┬────┘  └────┬─────┘  └────┬─────┘  └────┬───┘ │
│       │            │             │              │             │      │
│  ┌────┴────────────┴─────────────┴──────────────┴─────────────┴───┐ │
│  │                    SecurityConfig / CorsConfig                  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│  ┌────────┐  ┌──────────┐  ┌────────────┐  ┌───────────────────┐   │
│  │ Admin  │  │Analytics │  │ Insights   │  │ Resume            │   │
│  │Controller│  │Controller│  │ Controller │  │ Controller        │   │
│  │ Service │  │ Service  │  │ Service    │  │ Service           │   │
│  └────────┘  └──────────┘  └────────────┘  │ FileStorage       │   │
│                                             └───────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │  Common: ApiResponse<T>, PagedResponse<T>, GlobalExceptionHandler││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

**Tool**: Mermaid classDiagram or componentDiagram

---

## 4. Entity-Relationship Diagram (ERD)

**Audience**: Database developers, backend developers

**What to show**: All tables, columns, foreign keys, and relationships.

### Tables and Relationships

```
users
├── id (UUID PK)
├── email (UNIQUE)
├── password_hash
├── full_name
├── role (STUDENT|PO|RECRUITER|ADMIN)
├── is_active
└── timestamps
      │
      ├── student_profiles (1:1)
      │   └── user_id FK → users.id
      │
      ├── recruiter_profiles (1:1)
      │   └── user_id FK → users.id
      │
      ├── placement_officer_profiles (1:1)
      │   └── user_id FK → users.id
      │
      ├── drives (1:N, created_by)
      │   └── created_by FK → users.id
      │
      ├── job_posts (1:N, recruiter_id)
      │   └── recruiter_id FK → users.id
      │
      ├── applications (1:N, student_id)
      │   └── student_id FK → users.id
      │
      ├── notifications (1:N)
      │   └── user_id FK → users.id (CASCADE)
      │
      └── parsed_resumes (1:1)
          └── student_id FK → users.id

drives
└── id (UUID PK)
      │
      └── job_posts (1:N)
          └── drive_id FK → drives.id (CASCADE)
              │
              └── applications (1:N)
                  └── job_post_id FK → job_posts.id (CASCADE)
```

**Tool**: dbdiagram.io, Draw.io, or Mermaid erDiagram

---

## 5. Frontend Component Tree

**Audience**: Frontend developers

**What to show**: React component hierarchy and data flow.

### Page Route Tree

```
<QueryClientProvider>
  <Toaster />
  <ErrorBoundary>
    <BrowserRouter>
      <Routes>
        ├── /login → LoginPage
        ├── /register → RegisterPage
        ├── <ProtectedRoute>
        │   └── <AppLayout>                    [Sidebar navigation]
        │       ├── /dashboard → DashboardPage
        │       │   ├── StudentDashboard
        │       │   ├── RecruiterDashboard
        │       │   ├── PODashboard
        │       │   └── AdminDashboard
        │       ├── /drives → DrivesPage
        │       │   ├── DriveCard[]
        │       │   ├── DriveForm (modal)
        │       │   ├── SearchInput
        │       │   └── Pagination
        │       ├── /drives/:id → DriveDetailPage
        │       │   ├── DriveForm (edit)
        │       │   ├── JobPostCard[]
        │       │   │   ├── JobPostForm (inline edit)
        │       │   │   ├── JobPostStatusBadge
        │       │   │   └── ApplicationStatusBadge[]
        │       │   └── Pagination
        │       ├── /applications → ApplicationsPage
        │       │   ├── ApplicationStatusBadge[]
        │       │   └── Pagination
        │       ├── /notifications → NotificationsPage
        │       ├── /profile → ProfilePage
        │       │   └── StudentProfileSection / RecruiterProfileSection / POProfileSection
        │       ├── /admin/users → AdminUsersPage
        │       ├── /insights → JobPostInsights
        │       │   ├── ScoreDistributionChart
        │       │   ├── FunnelChart
        │       │   └── SkillGapTable
        │       ├── /analytics → AnalyticsDashboard
        │       │   ├── OverviewCards
        │       │   ├── FunnelChart
        │       │   └── DrivePerformanceTable
        │       └── /jobs/:jobPostId/rankings → JobPostRankings
        │           └── RankingsTable
        ├── / → redirect to /dashboard
        └── * → NotFoundPage
    </Routes>
  </ErrorBoundary>
</QueryClientProvider>
```

**Data Flow**:
- **Server state**: TanStack React Query → Axios → Spring Boot API → PostgreSQL
- **Client state**: Zustand store (auth) persisted to localStorage
- **Form state**: react-hook-form + zod validation

**Tool**: Mermaid graph TD or Lucidchart

---

## 6. Authentication Flow Diagram

**Audience**: All developers, security reviewers

**What to show**: JWT authentication lifecycle.

```
┌─────────┐          ┌──────────────┐          ┌────────────┐
│ Browser │          │ Spring Boot  │          │ PostgreSQL │
└────┬────┘          └──────┬───────┘          └─────┬──────┘
     │                     │                        │
     │  POST /auth/login   │                        │
     │  {email, password}  │                        │
     ├────────────────────▶│                        │
     │                     │   SELECT * FROM users  │
     │                     ├───────────────────────▶│
     │                     │◀───────────────────────┤
     │                     │                        │
     │                     │  BCrypt.verify(password)
     │                     │  JwtTokenProvider      │
     │                     │  .generateToken(user)  │
     │                     │                        │
     │  {token, user}      │                        │
     │◀────────────────────┤                        │
     │                     │                        │
     │                     │                        │
     │  GET /api/v1/...    │                        │
     │  Authorization:     │                        │
     │  Bearer <token>     │                        │
     ├────────────────────▶│                        │
     │                     │  JwtAuthenticationFilter│
     │                     │  .validateToken(token) │
     │                     │  → SecurityContext     │
     │                     │                        │
     │  {data}             │                        │
     │◀────────────────────┤                        │
     │                     │                        │
     │  401 (invalid/      │                        │
     │  expired token)     │                        │
     │◀────────────────────┤                        │
```

**Tool**: Mermaid sequenceDiagram

---

## 7. Application Lifecycle State Machine

**Audience**: Product owners, QA, developers

**What to show**: Valid application status transitions.

```
                    ┌──────────┐
                    │  APPLIED │
                    └────┬─────┘
                         │
                    ┌────▼────────┐
                    │ UNDER_REVIEW │
                    └────┬────────┘
                         │
                    ┌────▼──────────┐
                    │  SHORTLISTED  │
                    └────┬──────────┘
                         │
               ┌─────────┴─────────┐
               │                   │
         ┌─────▼──────┐    ┌──────▼──────┐
         │  ACCEPTED  │    │  REJECTED   │  (terminal states)
         └────────────┘    └─────────────┘

  WITHDRAWN ← can transition from any non-terminal state (APPLIED only in current impl)
```

**Drive status flow**: DRAFT → ACTIVE → CLOSED → COMPLETED
**Job post status flow**: OPEN → FILLED | CANCELLED

**Tool**: Mermaid stateDiagram-v2

---

## Recommended Implementation Order

1. **System Context Diagram** (Mermaid, embed in README)
2. **Container Diagram** (Mermaid, embed in README)
3. **ERD** (dbdiagram.io → export PNG, include in docs/)
4. **Auth Flow** (Mermaid sequenceDiagram, docs/AUTH_FLOW.md)
5. **Component Tree** (Mermaid graph, docs/COMPONENT_TREE.md)
6. **Backend Components** (Mermaid block diagram, docs/BACKEND_ARCH.md)

Generate all Mermaid diagrams as markdown code blocks — they render natively on GitHub and can be exported to SVG via the Mermaid CLI for embedding in presentation decks.
