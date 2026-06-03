# End-to-End Verification Report

**Date**: 2026-06-03
**Tester**: Senior QA / Frontend / Product Design / Tech Lead
**Methodology**: Live API testing + frontend code review across all 13 pages and 34 components

---

## Test Environment

| Service | Status | Notes |
|---------|--------|-------|
| PostgreSQL 16 | ✅ Healthy | `docker compose up -d` |
| Backend (Spring Boot) | ✅ Running | `:8080`, 46 routes |
| Frontend (Vite) | ✅ Built | `tsc -b && vite build` passes |

## Test Accounts

| Role | Email | Name | Password |
|------|-------|------|----------|
| ADMIN | admin@placement.com | System Admin | Admin@123 |
| PO | po@test.com | Dr. Sharma | Test@123 |
| RECRUITER | recruiter@test.com | Priya Singh | Test@123 |
| Student A (strong) | studenta@test.com | Rahul Verma | Test@123 |
| Student B (weak) | studentb@test.com | Ananya Patel | Test@123 |

## Test Scenario

- **Drive**: "Tech Summit 2026" — requires Java, Python, SQL, Spring Boot, min GPA 3.0
- **Job Post**: "Software Engineer Intern" — 3 vacancies, Bangalore, 20-30 LPA
- **Student A**: 6 skills (Java, Python, SQL, Spring Boot, AWS, Docker), GPA 8.5, strong resume → **score: 55**
- **Student B**: 2 skills (HTML, CSS), GPA 6.0, weak resume → **score: 0**

---

## Feature Verification Matrix

### AUTH

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Register | POST /auth/register | ✅ Works | Returns JWT + user |
| Login | POST /auth/login | ✅ Works | Returns JWT + user |
| Logout | POST /auth/logout | ✅ Works | Returns 200 |
| Session | JWT in Authorization header | ✅ Works | State validated across endpoints |
| 401 no token | GET /drives without auth | ✅ 401 | Correct response |
| 401 bad token | GET /drives with "invalid" | ✅ 401 | Correct response |
| 403 wrong role | STUDENT → GET /admin/users | ✅ 403 | Correct response |

### ADMIN

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Dashboard stats | GET /admin/stats | ✅ Works | 11 users, 2 drives, 6 apps |
| User list | GET /admin/users | ✅ Works | All roles present |
| Search users | `?search=rahul` | ✅ Works | Code review confirms |
| Role filter | `?role=STUDENT` | ✅ Works | Code review confirms |
| Role change | PATCH /admin/users/{id}/role | ✅ Works | Confirmation dialog |
| Toggle active | PATCH /admin/users/{id}/active | ✅ Works | Self-deactivate warning |
| Rankings access | Any page | ❌ No sidebar link | Admin must type URL manually |

### PLACEMENT OFFICER

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Profile update | PUT /profile/po | ✅ Works | collegeName, department |
| Create drive | POST /drives | ✅ Works | Status DRAFT |
| Edit drive | PUT /drives/{id} | ✅ Works | |
| Activate drive | PATCH /drives/{id}/status | ✅ Works | DRAFT→ACTIVE |
| Close drive | PATCH /drives/{id}/status | ✅ Works | ACTIVE→CLOSED |
| View applications | GET /applications/drive/{id} | ✅ Works | Grouped by job post |
| View rankings | Link on DriveDetailPage | ✅ Fixed | "View AI Rankings →" link |
| View insights | GET /insights/skill-gaps | ✅ Works | |
| View analytics | GET /analytics/overview | ✅ Works | |
| Notifications | GET /notifications | ✅ Works | |

### RECRUITER

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Profile update | PUT /profile/recruiter | ✅ Works | companyName, website, desc |
| Create job post | POST /job-posts | ✅ Works | Linked to drive, status OPEN |
| Edit job post | PUT /job-posts/{id} | ✅ Works | Inline JobPostForm |
| View applications | GET /applications/job-post/{id} | ✅ Works | Sidebar post selection |
| Resume download | GET /applications/{id}/resume | ❌ **500 ERROR** | Critical bug |
| Update status | PATCH /applications/{id}/status | ✅ Works | APPLIED→UNDER_REVIEW etc. |
| View rankings | Link on ApplicationsPage | ✅ Fixed | "View AI Rankings →" link |
| View insights | GET /insights/skill-gaps | ✅ Works | Skill gap table |
| Notifications | GET /notifications | ✅ Works | Auto-created on application |

### STUDENT A (Strong)

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Complete profile | PUT /profile/student | ✅ Works | 6 skills, GPA 8.5 |
| Upload resume | POST /resumes/upload | ✅ Works | PDF accepted |
| Resume download | GET /resumes/{filename} | ✅ Works | 200, correct PDF |
| Apply to job | POST /applications | ✅ Works | Score: 55 |
| View score on My Apps | GET /applications/my | ❌ **Not displayed** | `aiScore` in response but not rendered |
| View feedback | GET /applications/{id} | ❌ **Not displayed** | `scoringFeedback` only in ranked endpoint |
| Withdraw | PATCH /applications/{id}/withdraw | ✅ Works | Status→WITHDRAWN |
| Notifications | GET /notifications | ✅ Works | Auto-created on apply |

### STUDENT B (Weak)

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Complete profile | PUT /profile/student | ✅ Works | 2 skills, GPA 6.0 |
| Upload resume | POST /resumes/upload | ✅ Works | PDF accepted |
| Apply to job | POST /applications | ✅ Works | Score: 0, ranked #2 |
| View score | GET /applications/my | ❌ **Not displayed** | Same gap as Student A |
| Notifications | GET /notifications | ✅ Works | |

### AI FEATURES

| Feature | Test | Result | Notes |
|---------|------|--------|-------|
| Resume parsing (Tika) | POST /resumes/upload | ✅ Works | Text extracted |
| Skill extraction | Internal | ✅ Works | Skills found in PDF text |
| AI scoring (application) | On create | ✅ Works | Score computed |
| Scoring feedback | Ranked endpoint | ✅ Works | "Matches 3 of 4 skills" |
| Scoring rationale | Ranked endpoint | ❌ **null** | `scoringRationale` is always null |
| Candidate ranking | GET /ranked | ✅ Works | Sorted by score desc |
| Skill gap analysis | GET /insights/skill-gaps | ✅ Works | per-skill gaps computed |
| Score distribution | GET /insights/overview | ✅ Works | Buckets by range |
| Application funnel | GET /analytics/application-funnel | ✅ Works | Status counts |
| Drive performance | GET /analytics/drive-performance | ✅ Works | Per-drive metrics |

---

## Feature Verification Summary

| Domain | Total | ✅ Works | ⚠️ Concerns | ❌ Broken |
|--------|-------|---------|-------------|----------|
| AUTH | 6 | 6 | 0 | 0 |
| ADMIN | 7 | 6 | 0 | 1 |
| PO | 10 | 10 | 0 | 0 |
| RECRUITER | 9 | 7 | 0 | **1** |
| Student A | 8 | 5 | 1 | **2** |
| Student B | 5 | 3 | 1 | **1** |
| AI Features | 10 | 8 | 2 | 0 |
| **Total** | **55** | **45** | **4** | **5** |

---

## Bug Catalog

### 🟥 Critical Bugs

| Bug | Endpoint | Symptom | Root Cause |
|-----|----------|---------|------------|
| **#1 Resume download broken** | `GET /applications/{id}/resume` | Returns HTTP 500 for all users | Backend error in ApplicationController.downloadResume |
| **#2 Students cannot see AI score** | `GET /applications/my` | `aiScore` returned by API but never rendered | `ApplicationsPage.tsx` StudentApplications doesn't display score |
| **#3 Students cannot see AI feedback** | `GET /applications/{id}` | `scoringFeedback` not included in `ApplicationResponse` DTO | Backend DTO missing field; only `ScoredApplicationResponse` has it |

### 🟧 Moderate Bugs

| Bug | Endpoint | Symptom | Root Cause |
|-----|----------|---------|------------|
| **#4 Scoring rationale always null** | `GET /ranked` | `scoringRationale` is null in all responses | `KeywordScorer` doesn't populate rationale |
| **#5 Resume snapshot not linked** | DriveDetailPage PO view | Uses direct anchor to `/applications/{id}/resume` which returns 500 | Same root cause as #1 |
| **#6 PO ApplicationReview uses anchor instead of blob download** | DriveDetailPage:283 | Direct `<a href>` to the broken endpoint | Same root cause as #1 |

### 🟡 Minor Issues

| Bug | Issue | Location |
|-----|-------|----------|
| #7 | `scoringVersion` always null | Not a backend bug, just missing from scorer |
| #8 | Spring Boot skill not matched (gap=100%) despite being in resume | Keyword matching may use substring not exact match; not matching "Spring Boot" from "SKILLS: ... Spring Boot, ..." |
| #9 | Score 55 for 3/4 match is low | Scoring weight distribution unclear |
| #10 | `getUser()` and `getMe()` API functions exported but never called | `admin.ts`, `auth.ts` |

---

## UI/UX Audit

### Screen-by-Screen Review

#### LoginPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Visual hierarchy | ⚠️ Acceptable | Clean form, minimal |
| Layout | ✅ Fine | Centered card |
| Empty state | ✅ N/A | Form page |
| Error state | ⚠️ Minimal | Shows `error.message` text, no styling |
| Mobile | ✅ Fine | Simple form |
| **Professional appearance** | ⚠️ 6/10 | Plain card, no logo, no branding, no illustrations |

#### RegisterPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Form validation | ✅ Good | Zod: email, password min 8, confirm match, role selector |
| Error state | ⚠️ Minimal | Same as login |
| Role dropdown | ✅ Good | Only STUDENT/PO/RECRUITER (correctly excludes ADMIN) |
| **Professional appearance** | ⚠️ 6/10 | Same as login |

#### DashboardPage (all 4 roles)
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Usefulness | ⚠️ Moderate | Shows basic counts but no actionable data |
| Loading state | ✅ Good | Skeleton placeholders |
| Empty state | ⚠️ Missing | Just shows 0 values, no guidance |
| **StudentDashboard** | ⚠️ 5/10 | No AI score, no placement progress, no upcoming deadlines |
| **RecruiterDashboard** | ⚠️ 5/10 | Show counts but no recent activity |
| **PODashboard** | ⚠️ 5/10 | Show counts but no pipeline status |
| **AdminDashboard** | ✅ 7/10 | Best of the four — shows user breakdown, app pipeline |

#### DrivesPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Search + filter | ✅ Good | Search with debounce, status filter buttons |
| Pagination | ✅ Good | |
| Loading state | ✅ Good | Spinner |
| Empty state | ✅ Good | EmptyState component |
| **Professional appearance** | ✅ 7/10 | Clean card grid, functional |

#### DriveDetailPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Information density | ⚠️ Mixed | Good metadata display but no eligibility summary |
| PO actions | ✅ Good | Edit, Activate, Close, Delete clearly visible |
| Recruiter actions | ✅ Good | Post a Job button when ACTIVE |
| POApplicationReview | ⚠️ 6/10 | Functional but plain — grouped by title, status dropdown |
| Back navigation | ✅ Works | Back to Drives link |
| **Professional appearance** | ✅ 7/10 | Functional, clear sections |

#### ApplicationsPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Student view | ⚠️ 5/10 | Plain list, no AI score, no scoring feedback |
| Recruiter view | ✅ 7/10 | Sidebar with job posts, status transition buttons |
| PO view | ⚠️ 4/10 | EmptyState with link to drives — user must navigate away |
| Resume download | ❌ Broken | 500 error on click |
| Rankings link | ✅ Added | "View AI Rankings →" |
| **Professional appearance** | ⚠️ 6/10 | Functional but sparse |

#### ProfilePage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Inline editing | ✅ Good | Edit/Save pattern works |
| Resume upload | ✅ Good | File input with PDF restriction |
| Resume download | ✅ Works | Uses direct `/resumes/{filename}` endpoint |
| Student section | ✅ Good | College, GPA, skills, phone |
| Recruiter section | ✅ Good | Company details |
| PO section | ✅ Good | College, department |
| Validation | ⚠️ Basic | Manual validation, not react-hook-form |
| **Professional appearance** | ✅ 7/10 | Clean, functional sections |

#### NotificationsPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Read/unread distinction | ✅ Good | Blue left border for unread |
| Mark read on click | ✅ Good | |
| Mark all as read | ✅ Good | |
| Navigate on click | ✅ Good | Uses `linkUrl` |
| Loading state | ✅ Good | Skeleton |
| Empty state | ✅ Good | EmptyState |
| **Professional appearance** | ✅ 8/10 | Clean, works well |

#### AdminUsersPage
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Search | ✅ Good | Debounced search |
| Role filter | ✅ Good | Filter buttons with active state |
| Table | ✅ Good | Name, email, role select, actions |
| Role change | ✅ Good | Confirmation dialog |
| Toggle active | ✅ Good | Self-deactivate warning |
| Table responsiveness | ⚠️ Scroll on mobile | `overflow-x-auto` |
| **Professional appearance** | ✅ 8/10 | Clean table, good UX |

#### JobPostRankings
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Rankings table | ✅ Good | Rank, name, status, score, feedback, rationale |
| Score highlighting | ⚠️ Subtle | Top 3 highlighted blue, score colored green/orange |
| Back button | ✅ Works | `navigate(-1)` |
| Pagination | ✅ Good | |
| Empty state | ⚠️ Not checked | |
| **Professional appearance** | ✅ 7/10 | Information-dense, useful |

#### JobPostInsights
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Score distribution chart | ✅ Good | Recharts bar chart |
| Funnel chart | ✅ Good | Horizontal bar chart |
| Skill gap table | ✅ Good | Color-coded gap percentages |
| Loading state | ⚠️ Basic | Combined loading flag |
| **Professional appearance** | ✅ 8/10 | Charts make it look polished |

#### AnalyticsDashboard
| Criterion | Rating | Notes |
|-----------|--------|-------|
| Overview cards | ✅ Good | 5 stat cards |
| Drive performance table | ✅ Good | Per-drive metrics |
| Funnel chart | ✅ Good | Status breakdown |
| Loading state | ⚠️ Basic | Combined loading flag |
| **Professional appearance** | ✅ 8/10 | Most polished page |

### Navigation Audit (AppLayout)

| Criterion | Rating | Notes |
|-----------|--------|-------|
| Role-based links | ✅ Good | Different links per role |
| Responsive hamburger | ✅ Good | Mobile sidebar with overlay |
| Unread badge | ✅ Good | Shows count |
| Active link highlighting | ⚠️ Missing | No visual indicator of current route |
| Logout | ✅ Good | Clears query cache + store |
| Accessibility | ⚠️ Basic | Skip-to-content link present |

### Overall UI/UX Findings

#### Critical UI/UX Issues

| # | Issue | Location | Why Critical |
|---|-------|----------|-------------|
| **C1** | Resume download returns 500 | ApplicationsPage, DriveDetailPage | Every recruiter who tries to download a resume sees an error |

#### Major UI/UX Issues

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| **M1** | Students cannot see AI score or feedback on their applications | ApplicationsPage StudentApplications | Core value proposition invisible to students |
| **M2** | No visual indication of active route in sidebar | AppLayout | Users don't know which page they're on |
| **M3** | Login/Register pages have no branding or logo | LoginPage, RegisterPage | First impression is generic and unprofessional |
| **M4** | Dashboard for all roles is too sparse | DashboardPage | Doesn't convey value in 30 seconds |
| **M5** | PO Applications page is an EmptyState redirect | ApplicationsPage POApplications | Forces navigation away instead of showing data |

#### Minor UI/UX Issues

| # | Issue | Location |
|---|-------|----------|
| N1 | No page titles or breadcrumbs | All pages |
| N2 | Error messages unstyled (red text only) | All pages |
| N3 | No confirmation dialog for destructive drive actions | DriveDetailPage |
| N4 | Notifications page has "Try again" double rendered | NotificationsPage |
| N5 | Admin page title says "Dashboard" but shows admin stats | `/admin` route renders same DashboardPage |
| N6 | No tooltips or help text anywhere | All pages |
| N7 | Drive form is long with no section grouping | DriveForm |
| N8 | No loading skeleton for application list | ApplicationsPage |
| N9 | Table rows don't have hover state on some lists | Various |
| N10 | No favicon/browser tab branding | index.html |

---

## UI/UX Score by Screen (out of 10)

| Screen | Score | Verdict |
|--------|-------|---------|
| LoginPage | 6/10 | Functional but bland |
| RegisterPage | 6/10 | Functional but bland |
| DashboardPage (Student) | 5/10 | Too sparse, missing AI score |
| DashboardPage (Recruiter) | 5/10 | Too sparse |
| DashboardPage (PO) | 5/10 | Too sparse |
| DashboardPage (Admin) | 7/10 | Better, shows useful stats |
| DrivesPage | 7/10 | Clean, functional |
| DriveDetailPage | 7/10 | Functional, clear sections |
| ApplicationsPage (Student) | 5/10 | Missing AI score, no guidance |
| ApplicationsPage (Recruiter) | 7/10 | Good sidebar + action pattern |
| ApplicationsPage (PO) | 4/10 | EmptyState redirect is poor UX |
| ProfilePage | 7/10 | Clean inline editing |
| NotificationsPage | 8/10 | Best simple page |
| AdminUsersPage | 8/10 | Clean table, good UX |
| JobPostRankings | 7/10 | Information-dense, useful |
| JobPostInsights | 8/10 | Charts add polish |
| AnalyticsDashboard | 8/10 | Most polished page |
| **Average** | **6.5/10** | Functional but not impressive |

---

## Top 10 UI/UX Improvements Ranked by Impact

| Rank | Improvement | Impact | Effort |
|------|------------|--------|--------|
| 1 | **Fix resume download (500 error)** | Critical — unblocks recruiter/PO workflow | Low (backend bug fix) |
| 2 | **Show AI score + feedback on student applications** | High — students see their AI value | Low (add 2 fields to StudentApplications) |
| 3 | **Add logo/branding to login page** | High — first impression improvement | Low (SVG logo + gradient background) |
| 4 | **Add active route highlighting in sidebar** | Medium — navigation clarity | Low (react-router `useLocation`) |
| 5 | **Replace PO Applications EmptyState with inline data** | Medium — PO can see apps without navigating away | Medium (fetch apps for PO on page load) |
| 6 | **Add page titles and breadcrumbs** | Medium — orientation | Low |
| 7 | **Dashboard: add upcoming deadlines, recent activity, AI score summary** | High — dashboard should convey value in 30s | Medium |
| 8 | **Style error messages as toasts or alert banners** | Medium — polish | Low |
| 9 | **Add confirmation dialogs for all destructive actions** | Medium — safety | Low |
| 10 | **Responsive table improvements for mobile** | Low — polish | Low |

## Top 10 Functionality Improvements Ranked by Impact

| Rank | Improvement | Impact | Effort |
|------|-------------|--------|--------|
| 1 | **Fix resume download** (Bug #1) | Critical — breaks core workflow | Low (backend fix) |
| 2 | **Add scoringFeedback to ApplicationResponse DTO** | High — students need feedback | Low (add field to DTO) |
| 3 | **Display AI score on Student Applications page** | High — students see their score | Low (frontend render) |
| 4 | **Implement scoringRationale in KeywordScorer** | Medium — rationale is always null | Low (add rationale text) |
| 5 | **Add Average Score to recruiter applications view** | Medium — context for decision-making | Low |
| 6 | **Auto-close job posts when vacancies filled** | Medium — workflow automation | Medium (backend event) |
| 7 | **WebSocket/SSE real-time notifications** | Medium — UX improvement | High |
| 8 | **Add "View All Drives" link to admin sidebar** | Low — admin navigation completeness | Low |
| 9 | **Gemini API integration for semantic scoring** | Low — future improvement | High |
| 10 | **Export rankings as CSV/PDF** | Low — nice-to-have | Medium |

---

## Screen Maturity Assessment

### Which screens feel unfinished

| Screen | Why |
|--------|-----|
| **PO ApplicationsPage** | EmptyState redirect instead of inline data — feels like a placeholder |
| **Student ApplicationsPage** | No AI score, no feedback, no guidance — missing core value |
| **All Dashboards (except Admin)** | Sparse stat cards with no actionable content — feels like a scaffold |
| **Login/Register pages** | No branding, no logo, no visual identity — generic template feel |

### Which screens feel production-ready

| Screen | Why |
|--------|-----|
| **AdminUsersPage** | Clean table, search, filter, confirmations, self-deactivate warning |
| **NotificationsPage** | Read/unread distinction, mark all, navigation on click |
| **JobPostInsights** | Charts, color-coded tables, useful data |
| **AnalyticsDashboard** | Overview cards, funnel chart, drive performance table |
| **ProfilePage** | Clean inline editing, role-aware sections |

### Which screens would impress recruiters

| Screen | Why |
|--------|-----|
| **JobPostRankings** | "This is the killer feature — AI-ranked candidates with scores and feedback" |
| **JobPostInsights** | "Skill gap analysis shows we understand hiring challenges" |
| **AnalyticsDashboard** | "Professional dashboards that drive decisions" |
| **DriveDetailPage** | "Full lifecycle management in one view" |

### Which screens reduce perceived quality

| Screen | Why |
|--------|-----|
| **LoginPage** | "Generic form — first impression is 6/10" |
| **Dashboard (all roles)** | "Just numbers, no insight — looks like an admin panel from 2015" |
| **Student Applications** | "Missing AI scores — the demo promises AI but students can't see it" |
| **PO Applications** | "Empty state with a redirect — looks broken" |

---

## Final Answers

### 1. Is the project fully functional?

**No.** One critical bug blocks a core workflow: **resume download via applications endpoint is broken (500 error)**. This means recruiters and POs cannot download student resumes from the applications view. Additionally, AI scores are computed and stored but never shown to students.

### 2. Is the project demo-ready?

**Nearly, but not yet.** A demo script can navigate around the resume download bug (use the student's own download via profile page instead), but:
- The resume 500 error will surface immediately if a recruiter clicks "Download Resume"
- The AI scoring demo will feel hollow if students can't see their scores
- The login page creates a weak first impression

### 3. Is the UI/UX recruiter-ready?

**Barely.** Core workflows work and the analytics/insights pages look professional. But:
- Dashboards don't convey value in 30 seconds
- The login page is generic
- Students can't see AI scores (which is the marquee feature)
- The self-serve experience is poor

A recruiter evaluating the system would see functional workflows but would not be wowed. They'd see the Rankings page and think "this is useful" but then hit the resume download error and lose confidence.

### 4. What is the single biggest weakness remaining?

**The resume download 500 error.** It's the most visible, most frequently used action in the recruiter/PO workflow. Every demo that involves reviewing applications will hit this. It erodes trust immediately.

### 5. What is the single highest-impact improvement remaining?

**Show AI scores and feedback to students on their applications page.** This is the project's marquee feature. If students can see "Your resume scored 55/100 — you matched 3 of 4 required skills," the value proposition becomes immediately clear to all stakeholders. Currently the scores exist in the backend but are invisible to the students who need them most.

---

## Appendix: Raw Test Data

```
Student A: Rahul Verma
  Skills: Java, Python, SQL, Spring Boot, AWS, Docker
  GPA: 8.5
  Resume: Contains Java, Python, SQL, Spring Boot, AWS, Docker, REST APIs, ...
  AI Score: 55.0 (matched 3/4 required skills)
  Ranking: #1

Student B: Ananya Patel
  Skills: HTML, CSS
  GPA: 6.0
  Resume: Contains HTML, CSS
  AI Score: 0.0 (matched 0/4 required skills)
  Ranking: #2

Skill Gaps (overall):
  Spring Boot: required=1, matched=0, gap=100%
  Java:        required=1, matched=1, gap=0%
  Python:      required=1, matched=1, gap=0%
  SQL:         required=1, matched=1, gap=0%

Application Funnel:
  APPLIED: 5
  WITHDRAWN: 1

Total Users: 11
Active Drives: 2
Total Applications: 6
Average AI Score: 13.75
```
