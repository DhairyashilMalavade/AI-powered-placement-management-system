# Phase 5 — AI Resume Parsing, Scoring, Ranking, Insights & Analytics

## Implementation Status

| Feature | Status |
|---|---|
| Resume Parsing (Apache Tika) | Planned |
| Resume Keyword Scoring (KeywordScorer) | Planned |
| Candidate Ranking | Planned |
| Recruiter Insights | Planned |
| Analytics Dashboard | Planned |
| Gemini API integration (future) | Not yet implemented |
| Async scoring (event-driven) | Not yet implemented |

All items listed as *Planned* are in scope for Phase 5. Items listed as *Not yet implemented* are future enhancements documented for awareness.

---

## Corrections Applied (from Architecture Review)

| # | Finding | Severity | Correction |
|---|---|---|---|
| 1 | Migration number was V14, latest is V15 | Major | Renamed to **V16** |
| 2 | `VARCHAR(2000)` is too restrictive for extracted skills/education | Major | Changed to **TEXT** |
| 3 | `UNIQUE(student_id)` already creates an index on `parsed_resumes` | Major | Removed redundant `CREATE INDEX` |
| 4 | `scoring_version` needs a default to distinguish unscored apps | Minor | Added `DEFAULT 'keyword-v1'` |
| 5 | `toResponse()` has no role context, cannot filter scoring fields | Critical | New **`ScoredApplicationResponse`** DTO; existing `ApplicationResponse` untouched |
| 6 | Ranked-list visibility must differ per role (STUDENT/RECRUITER/PO/ADMIN) | Major | Single endpoint with role-gated response builder |
| 7 | `AIScorer` interface was over-coupled (Application + JobPost) | Major | Simplified to `score(Drive, ParsedResume, StudentProfile)` |
| 8 | Synchronous scoring blocks HTTP if AI API is added later | Major | Documented as future async work; not implemented |
| 9 | `extracted_skills` is TEXT but `drive.requiredSkills` is `String[]` | Minor | Documented Java-level conversion |

---

## Final Architecture

### Backend Packages (new + modified)

```
backend/src/main/java/.../Placement_management_system/

  ai/                                    ← NEW PACKAGE
  ├── ResumeParser.java                  interface
  ├── ResumeParseResult.java             DTO
  ├── TikaResumeParser.java              implementation
  ├── AIScorer.java                      interface
  ├── ScoringResult.java                 DTO
  └── KeywordScorer.java                 implementation

  application/
  ├── Application.java                   +3 fields (scoringRationale, scoringFeedback, scoringVersion)
  ├── ApplicationService.java            +scoring trigger, +ranked-list method
  ├── ApplicationController.java         +ranked endpoint
  ├── ApplicationRepository.java         +2 queries
  └── dto/
      ├── ApplicationResponse.java       UNCHANGED
      ├── CreateApplicationRequest.java  UNCHANGED
      └── ScoredApplicationResponse.java  NEW flat DTO

  resume/
  ├── ResumeController.java              +parse trigger on upload (via new service)
  ├── FileStorageService.java            UNCHANGED
  ├── LocalFileStorageService.java       UNCHANGED
  └── ResumeService.java                 ← NEW — coordinates upload + parse + storage

  user/
  └── ParsedResume.java                  ← NEW entity
  └── ParsedResumeRepository.java        ← NEW repository

  insights/                              ← NEW PACKAGE
  ├── InsightsController.java
  ├── InsightsService.java
  └── dto/ (SkillGapDTO, ScoreDistributionDTO, JobPostOverviewDTO)

  analytics/                             ← NEW PACKAGE
  ├── AnalyticsController.java
  ├── AnalyticsService.java
  └── dto/ (OverviewDTO, DrivePerformanceDTO, FunnelDTO)
```

### Frontend Pages (new)

```
frontend/src/
  pages/
  ├── JobPostRankings.tsx                Route: /jobs/:jobPostId/rankings
  ├── JobPostInsights.tsx                Route: /jobs/:jobPostId/insights
  └── AnalyticsDashboard.tsx             Route: /analytics
  components/
  ├── RankingsTable.tsx
  ├── ScoreDistributionChart.tsx
  ├── FunnelChart.tsx
  ├── SkillGapTable.tsx
  └── OverviewCards.tsx
  hooks/
  └── useAnalyticsStore.ts               Zustand store for analytics caching
```

---

## Database Schema Changes

### New Table: `parsed_resumes`

```sql
CREATE TABLE parsed_resumes (
    id BIGSERIAL PRIMARY KEY,
    student_id UUID NOT NULL UNIQUE REFERENCES users(id),
    resume_file_path VARCHAR(500) NOT NULL,
    extracted_text TEXT,
    extracted_skills TEXT,              -- comma-separated skill names
    extracted_experience_years INTEGER,
    extracted_education TEXT,           -- comma-separated degree entries
    parse_version VARCHAR(50) NOT NULL DEFAULT 'tika-v1',
    parsed_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Index on student_id is auto-created by UNIQUE constraint
```

### Modified Table: `applications` (3 new columns)

```sql
ALTER TABLE applications
    ADD COLUMN scoring_rationale TEXT,
    ADD COLUMN scoring_feedback TEXT,
    ADD COLUMN scoring_version VARCHAR(50) DEFAULT 'keyword-v1';
```

### New Index

```sql
CREATE INDEX idx_applications_job_post_score
    ON applications(job_post_id, ai_score DESC);
```

---

## Migration Plan (V16)

**File**: `backend/src/main/resources/db/migration/V16__add_parsed_resumes_and_scoring_fields.sql`

```sql
-- 1. New table for parsed resume data
CREATE TABLE parsed_resumes (
    id BIGSERIAL PRIMARY KEY,
    student_id UUID NOT NULL UNIQUE REFERENCES users(id),
    resume_file_path VARCHAR(500) NOT NULL,
    extracted_text TEXT,
    extracted_skills TEXT,
    extracted_experience_years INTEGER,
    extracted_education TEXT,
    parse_version VARCHAR(50) NOT NULL DEFAULT 'tika-v1',
    parsed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. New columns on applications
ALTER TABLE applications
    ADD COLUMN scoring_rationale TEXT,
    ADD COLUMN scoring_feedback TEXT,
    ADD COLUMN scoring_version VARCHAR(50) DEFAULT 'keyword-v1';

-- 3. Index for ranked-list queries
CREATE INDEX idx_applications_job_post_score
    ON applications(job_post_id, ai_score DESC);
```

**Rollback**:
```sql
DROP TABLE IF EXISTS parsed_resumes;
ALTER TABLE applications DROP COLUMN IF EXISTS scoring_rationale;
ALTER TABLE applications DROP COLUMN IF EXISTS scoring_feedback;
ALTER TABLE applications DROP COLUMN IF EXISTS scoring_version;
DROP INDEX IF EXISTS idx_applications_job_post_score;
```

---

## Entity Design

### `ParsedResume.java` (new entity)

```java
@Entity
@Table(name = "parsed_resumes")
@Getter @Setter @NoArgsConstructor
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;

    @Column(name = "resume_file_path", length = 500, nullable = false)
    private String resumeFilePath;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    @Column(name = "extracted_experience_years")
    private Integer extractedExperienceYears;

    @Column(name = "extracted_education", columnDefinition = "TEXT")
    private String extractedEducation;

    @Column(name = "parse_version", length = 50, nullable = false)
    private String parseVersion = "tika-v1";

    @Column(name = "parsed_at", nullable = false, updatable = false)
    private LocalDateTime parsedAt;

    @PrePersist
    protected void onCreate() {
        parsedAt = LocalDateTime.now();
    }
}
```

### `Application.java` — 3 new fields

```java
@Column(name = "scoring_rationale", columnDefinition = "TEXT")
private String scoringRationale;

@Column(name = "scoring_feedback", columnDefinition = "TEXT")
private String scoringFeedback;

@Column(name = "scoring_version", length = 50)
private String scoringVersion;
```

---

## DTO Design

### `ScoredApplicationResponse.java` (new, flat)

```java
@Getter @Setter @NoArgsConstructor
public class ScoredApplicationResponse {
    private UUID applicationId;
    private String studentName;
    private String studentId;
    private String status;
    private BigDecimal aiScore;
    private String scoringFeedback;
    private Integer rank;

    // RECRUITER/PO/ADMIN only — null for STUDENT
    private String scoringRationale;
    private String scoringVersion;
}
```

### `ApplicationResponse.java` (unchanged)

No modifications. Existing endpoints continue to return this DTO.

### `ScoringResult.java` (new, internal)

```java
@Getter @Setter @AllArgsConstructor
public class ScoringResult {
    private int score;             // 0–100
    private String rationale;      // detailed (recruiter-facing)
    private String feedback;       // high-level (student-facing)
    private String version;        // e.g. "keyword-v1"
}
```

### `ResumeParseResult.java` (new, internal)

```java
@Getter @Setter
public class ResumeParseResult {
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private int experienceYears;
    private List<String> education;
}
```

---

## API Endpoints

### New Endpoints

| Method | Endpoint | PreAuthorize | Returns | Description |
|---|---|---|---|---|
| `GET` | `/api/v1/job-posts/{jobPostId}/applications/ranked` | `isAuthenticated()` | `PagedResponse<ScoredApplicationResponse>` | Ranked list (role-filtered) |
| `GET` | `/api/v1/insights/skill-gaps` | `hasAnyRole('RECRUITER','PO')` | `ApiResponse<List<SkillGapDTO>>` | Required vs available skills |
| `GET` | `/api/v1/insights/overview` | `hasAnyRole('RECRUITER','PO')` | `ApiResponse<JobPostOverviewDTO>` | Score distribution + funnel |
| `GET` | `/api/v1/analytics/overview` | `hasRole('ADMIN')` | `ApiResponse<OverviewDTO>` | Platform-wide stats |
| `GET` | `/api/v1/analytics/drive-performance` | `hasRole('ADMIN')` | `ApiResponse<List<DrivePerformanceDTO>>` | Per-drive metrics |
| `GET` | `/api/v1/analytics/application-funnel` | `hasRole('ADMIN')` | `ApiResponse<FunnelDTO>` | Funnel across all drives |

### Modified Endpoints (existing)

| Endpoint | Change |
|---|---|
| `POST /api/v1/resumes/upload` | After file saved, auto-parse with Tika → store in `parsed_resumes` table |
| `POST /api/v1/applications` | After application saved, auto-score with `KeywordScorer` → set `aiScore`, rationale, feedback, version |

---

## Security Rules

### Role Hierarchy (Spring Security, existing)

`ADMIN > PO > RECRUITER > STUDENT`

Because of hierarchy, `@PreAuthorize("hasRole('STUDENT')")` matches all roles. The ranked-list endpoint uses **exact role checks** (manual authority inspection) for STUDENT access control.

### Visibility Matrix

| Field | STUDENT | RECRUITER | PO | ADMIN |
|---|---|---|---|---|
| `applicationId` | ✓ | ✓ | ✓ | ✓ |
| `studentName` | hidden (own name only) | ✓ | ✓ | ✓ |
| `status` | ✓ | ✓ | ✓ | ✓ |
| `aiScore` | ✓ | ✓ | ✓ | ✓ |
| `scoringFeedback` | ✓ | ✓ | ✓ | ✓ |
| `scoringRationale` | ✗ | ✓ | ✓ | ✓ |
| `scoringVersion` | ✗ | ✓ | ✓ | ✓ |
| `rank` | ✓ (own only) | ✓ (full list) | ✓ (full list) | ✓ (full list) |
| Skill gap analysis | ✗ | ✓ (own posts) | ✓ (own drives) | ✓ |
| Analytics dashboard | ✗ | ✗ | ✓ | ✓ |

### Access Control Logic (ranked-list endpoint)

```java
boolean isRecruiter = jobPost.getRecruiter().getId().equals(currentUserId);
boolean isPO = drive.getCreatedBy().getId().equals(currentUserId);
boolean isAdmin = hasExactRole(currentUser, "ROLE_ADMIN");

if (isRecruiter || isPO || isAdmin) {
    return fullRankedList(pageable);     // ScoredApplicationResponse with all fields
}

// STUDENT path
Application app = findByStudentIdAndJobPostId(currentUserId, jobPostId);
if (app == null) {
    throw new BusinessException("Access denied");
}
return singleEntry(app);                 // ScoredApplicationResponse with rationale=null
```

---

## AI Scoring Flow

### Architecture

```
┌──────────────┐     ┌────────────────┐     ┌──────────────┐
│  Application  │────▶│  AIScorer      │◀────│  Drive       │
│  Service      │     │  (interface)   │     │  (criteria)  │
│  .create()    │     │                │     └──────────────┘
└──────────────┘     │ score(         │
       │             │   Drive,       │     ┌──────────────┐
       │             │   ParsedResume,│◀────│  ParsedResume│
       │             │   StudentProfi)│     │  (candidate) │
       ▼             └───────┬────────┘     └──────────────┘
┌──────────────┐             │
│  Application │     ┌───────▼────────┐
│  (aiScore,   │◀────│  ScoringResult │
│   rationale, │     │  {score,       │
│   feedback,  │     │   rationale,   │
│   version)   │     │   feedback,    │
└──────────────┘     │   version}     │
                     └────────────────┘
```

### `AIScorer` Interface

```java
public interface AIScorer {
    ScoringResult score(Drive drive, ParsedResume parsedResume, StudentProfile profile);
}
```

### `KeywordScorer` Algorithm

```java
@Override
public ScoringResult score(Drive drive, ParsedResume parsedResume, StudentProfile profile) {
    // TEXT-to-array conversion: split extracted_skills (comma-separated TEXT)
    // then compare against drive.getRequiredSkills() (Java String[] → TEXT[])
    Set<String> required = new HashSet<>(Arrays.asList(drive.getRequiredSkills()));
    Set<String> candidate = parseSkillString(parsedResume.getExtractedSkills());

    int matched = intersection(required, candidate).size();
    int missing = required.size() - matched;

    // Score components: skills (60) + GPA (15) + experience (15) − penalties
    int skillScore = required.isEmpty() ? 0 : (int) ((double) matched / required.size() * 60);
    boolean gpaOk = profile.getGpa() != null && drive.getMinGpa() != null
                    && profile.getGpa().compareTo(drive.getMinGpa()) >= 0;
    int gpaScore = gpaOk ? 15 : 0;
    int expScore = (parsedResume.getExtractedExperienceYears() != null
                    && parsedResume.getExtractedExperienceYears() >= 1) ? 15 : 0;
    int penalty = missing * 5;

    int finalScore = Math.max(0, Math.min(100, skillScore + gpaScore + expScore - penalty));

    // Rationale (detailed — recruiter/PO/admin only)
    String rationale = String.format(
        "Matched %d/%d required skills. GPA %s. Score: %d/100.",
        matched, required.size(), gpaOk ? "OK" : "below minimum", finalScore);

    // Feedback (high-level — visible to student)
    String feedback = String.format(
        "Your resume matches %d of %d required skills. %s",
        matched, required.size(), gpaOk ? "" : "Your GPA is below the minimum requirement.");

    return new ScoringResult(finalScore, rationale, feedback, "keyword-v1");
}
```

### Scoring Trigger in `ApplicationService.create()`

```java
// After existing save logic (line 96):
try {
    ParsedResume parsed = parsedResumeRepository.findByStudentId(studentId).orElse(null);
    StudentProfile profile = studentProfileRepository.findByUserId(studentId).orElse(null);
    if (parsed != null && profile != null) {
        Drive drive = jobPost.getDrive();
        ScoringResult result = aiScorer.score(drive, parsed, profile);

        application.setAiScore(BigDecimal.valueOf(result.getScore()));
        application.setScoringRationale(result.getRationale());
        application.setScoringFeedback(result.getFeedback());
        application.setScoringVersion(result.getVersion());
        applicationRepository.save(application);

        notificationService.create(studentId, "Application Scored",
            "Your application for \"" + jobPost.getTitle() + "\" has been scored: "
            + result.getScore() + "/100.",
            "/applications/" + application.getId());
    }
} catch (Exception e) {
    log.warn("AI scoring failed for application {}", application.getId(), e);
    // Non-blocking — application was created successfully
}
```

### Non-blocking Design

Scoring failure does not prevent the application from being created. If scoring fails:
- `aiScore` remains `null`
- Application is still saved with `status = "APPLIED"`
- Error is logged
- The application will appear at the bottom of ranked lists (`NULLS LAST`)

---

## ParsedResume Design

### Storage

- One `parsed_resumes` row per student (`UNIQUE(student_id)`)
- Created/updated on every resume upload
- Stores the raw extracted text, comma-separated skills and education, and estimated experience years
- `parse_version` enables future parser upgrades (e.g., `tika-v1` → `gemini-v1`)

### Text-to-Array Conversion

`Drive.requiredSkills` is `String[]` (PostgreSQL `TEXT[]`), while `ParsedResume.extracted_skills` is `TEXT` (comma-separated string). The `KeywordScorer` converts both to `Set<String>` for matching:

```java
// TEXT → Set<String>
private Set<String> parseSkillString(String skills) {
    if (skills == null || skills.isBlank()) return Set.of();
    return Arrays.stream(skills.split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
}

// String[] → Set<String>
Set<String> required = Arrays.stream(drive.getRequiredSkills())
    .map(String::toLowerCase)
    .collect(Collectors.toSet());
```

### Resume Deletion

When a student uploads a new resume:
1. Old `parsed_resumes` row is deleted or updated (upsert by `student_id`)
2. Old file is deleted from `./uploads/`
3. New file is parsed and new `parsed_resumes` row created

---

## Ranking Design

### Query

```java
@Query("SELECT a FROM Application a JOIN FETCH a.student " +
       "WHERE a.jobPost.id = :jobPostId AND a.status <> 'WITHDRAWN' " +
       "ORDER BY a.aiScore DESC NULLS LAST")
Page<Application> findRankedByJobPostId(UUID jobPostId, Pageable pageable);
```

### Rank Computation

```java
private int computeRank(Application app) {
    if (app.getAiScore() == null) return -1;  // unscored
    long better = applicationRepository
        .countByJobPostIdAndAiScoreGreaterThan(app.getJobPost().getId(), app.getAiScore());
    return (int) better + 1;
}
```

### Index

`idx_applications_job_post_score(job_post_id, ai_score DESC)` covers the WHERE + ORDER BY. PostgreSQL can index-scan by `(job_post_id, ai_score)` and skip WITHDRAWN rows in a filter step.

### Student vs Recruiter View

- **RECRUITER/PO/ADMIN**: Paginated list of all applications sorted by score. Each entry includes `scoringRationale` and `scoringVersion`.
- **STUDENT**: Single entry (own application only). Only `aiScore`, `scoringFeedback`, and `rank` are populated. `scoringRationale` and `scoringVersion` are null.

---

## Analytics Design

### Overview

```sql
SELECT
    (SELECT COUNT(*) FROM drives) AS total_drives,
    (SELECT COUNT(*) FROM job_posts) AS total_job_posts,
    (SELECT COUNT(*) FROM applications) AS total_applications,
    (SELECT COUNT(*) FROM applications WHERE status = 'ACCEPTED') AS total_placements,
    (SELECT AVG(ai_score) FROM applications WHERE ai_score IS NOT NULL) AS average_score;
```

Single SQL query, aggregates across all tables. Uses full table scans (acceptable for <100K rows).

### Drive Performance

```sql
SELECT
    d.id, d.title,
    COUNT(DISTINCT jp.id) AS total_posts,
    COUNT(a.id) AS total_applicants,
    COUNT(a.id) FILTER (WHERE a.status = 'ACCEPTED') AS total_filled,
    AVG(a.ai_score) FILTER (WHERE a.ai_score IS NOT NULL) AS avg_score
FROM drives d
LEFT JOIN job_posts jp ON jp.drive_id = d.id
LEFT JOIN applications a ON a.job_post_id = jp.id
GROUP BY d.id, d.title;
```

### Application Funnel

```sql
SELECT
    a.status,
    COUNT(*) AS count
FROM applications a
GROUP BY a.status
ORDER BY a.status;
```

### Frontend Charts

- **ScoreDistributionChart**: Histogram with buckets 0–20, 21–40, 41–60, 61–80, 81–100
- **FunnelChart**: Stepped bar chart: APPLIED → UNDER_REVIEW → SHORTLISTED → ACCEPTED | REJECTED
- **SkillGapTable**: Required skill vs matched count vs gap percentage
- **OverviewCards**: Stat cards (drives, posts, applications, placements, avg score)

Library: `recharts` (add to frontend dependencies if not present).

---

## Visibility Rules (by Role)

### Ranked List Endpoint

```
Caller Role    ──►  RECRUITER/PO/ADMIN  ──► Full paginated list, all fields
               ──►  STUDENT              ──► Single entry (own), rationale/version = null
               ──►  Other                ──► 403 Forbidden
```

### Insights Endpoints

```
Caller Role    ──►  RECRUITER  ──► Their own job posts only
               ──►  PO         ──► Their own drives only
               ──►  ADMIN      ──► All data
               ──►  STUDENT    ──► 403 Forbidden
```

### Analytics Endpoints

```
Caller Role    ──►  ADMIN  ──► Full platform data
               ──►  PO     ──► Full platform data (placement officers manage placement)
               ──►  Other  ──► 403 Forbidden
```

---

## Test Strategy

### Backend Tests

| Test File | What It Tests | Command |
|---|---|---|
| `TikaResumeParserTest` | Parse a sample PDF, verify extracted fields | `./mvnw test -Dtest=TikaResumeParserTest` |
| `KeywordScorerTest` | Unit test scoring with known inputs (matched skills, GPA, experience) | `./mvnw test -Dtest=KeywordScorerTest` |
| `ApplicationServiceTest` | Scoring triggered on create, non-blocking on failure | `./mvnw test -Dtest=ApplicationServiceTest` |
| `ApplicationControllerTest` | Ranked list: student sees own, recruiter sees all | `./mvnw test -Dtest=ApplicationControllerTest` |
| `InsightsControllerTest` | Skill gaps, score distribution, role gating | `./mvnw test -Dtest=InsightsControllerTest` |
| `AnalyticsControllerTest` | Overview, drive performance, funnel | `./mvnw test -Dtest=AnalyticsControllerTest` |

**Test data setup**: Create a drive with required skills + min GPA, a recruiter, a job post, 3 students with different parsed resumes/GPA, 3 applications (one per student). Verify scoring, ranking order, role visibility.

### Frontend Tests

| Test File | What It Tests |
|---|---|
| `JobPostRankings.test.tsx` | Render ranked table, student sees only own row |
| `JobPostInsights.test.tsx` | Render charts with mock data |
| `AnalyticsDashboard.test.tsx` | Render stat cards and tables |

### Running Tests

```bash
cd backend && ./mvnw test              # All backend tests
cd frontend && npm test                # All frontend tests
```

---

## Known Future Improvements

| Item | Why Future | Trigger |
|---|---|---|
| **Gemini API scoring** | Requires API key, internet, ongoing cost; adds 1–3s latency | When semantic matching is needed beyond keyword scoring |
| **Async scoring** | Current sync scoring (~50ms) is acceptable; async adds event infrastructure complexity | When Gemini is integrated or scoring volume exceeds 100+ per minute |
| **Embedding-based scorer** | Requires pgvector extension or vector DB; overkill for keyword matching | When resume volume requires similarity search across thousands of candidates |
| **Resume re-parsing** | Current parse-on-upload is sufficient | When parser algorithm is upgraded and historical resumes need re-processing |
| **Batch re-scoring endpoint** | `POST /api/v1/applications/re-score` — iterates by `scoringVersion` | When a new scorer is deployed and existing scores need updating |
| **Student profile auto-fill** | Use parsed name/email/phone to suggest profile fields | After Phase 5 core is stable |
| **Analytics materialized views** | Current SQL aggregations are fast enough for <100K rows | When query latency exceeds 500ms |
| **PDF text caching** | `extracted_text` is already stored in `parsed_resumes` | N/A — already solved |

---

## Implementation Order

| # | Step | Files | Depends On |
|---|---|---|---|
| 1 | Add Tika + Flyway V16 dependencies to `pom.xml` | `pom.xml` | — |
| 2 | Create `ai/` package (Parser + Scorer interfaces + impls) | 6 new files | Step 1 |
| 3 | Create `ParsedResume` entity + repository + migration V16 | 3 new files | — |
| 4 | Create `ResumeService` — coordinate upload + parse + storage | 1 new file | Steps 2, 3 |
| 5 | Modify `Application` entity — add 3 scoring fields | `Application.java` | Step 3 |
| 6 | Create `ScoredApplicationResponse` DTO | 1 new file | — |
| 7 | Modify `ApplicationService.create()` — trigger scoring | `ApplicationService.java` | Steps 2, 5 |
| 8 | Add repository queries for ranking | `ApplicationRepository.java` | Step 5 |
| 9 | Add ranked-list endpoint + role-filtered response | `ApplicationController.java`, `ApplicationService.java` | Steps 6, 7, 8 |
| 10 | Create insights service + controller | 4 new files | Step 2 |
| 11 | Create analytics service + controller | 4 new files | — |
| 12 | Frontend: Rankings page | `JobPostRankings.tsx`, `RankingsTable.tsx` | Step 9 |
| 13 | Frontend: Insights page with charts | `JobPostInsights.tsx`, chart components | Step 10 |
| 14 | Frontend: Analytics dashboard | `AnalyticsDashboard.tsx`, stat components | Step 11 |
| 15 | Integration tests | Test files | Steps 1–14 |

---

## Critical Findings Verification

| Finding | Status |
|---|---|
| Ranking query needs `ORDER BY ai_score DESC NULLS LAST` with covering index | **RESOLVED** — V16 migration includes `idx_applications_job_post_score(job_post_id, ai_score DESC)`; JPQL uses explicit `ORDER BY a.aiScore DESC NULLS LAST` |
| `toResponse()` role-awareness for scoring fields | **RESOLVED** — New `ScoredApplicationResponse` DTO separate from `ApplicationResponse`; role-filtered in service layer, not in DTO construction |
| Hibernate `validate` compatibility | **NOT AN ISSUE** — Standard Flyway-first pattern; all new columns are nullable |

**Zero Critical findings remain.**
