# Demo Walkthrough Script

**Application**: AI-Powered Placement Management System
**Audience**: Project evaluators, stakeholders
**Duration**: ~10–12 minutes

---

## Setup (2 minutes)

Before the demo, ensure all services are running:

```bash
docker compose up -d postgres          # Start database
export JWT_SECRET="demo-secret"        # Set JWT secret
cd backend && ./mvnw spring-boot:run   # In one terminal
cd frontend && npm run dev             # In another terminal
```

**Browser tabs to open**:
- Tab 1: Student flow (incognito)
- Tab 2: Admin/PO flow
- Tab 3: Recruiter flow

**Seeded accounts**:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@placement.com | Admin@123 |
| PO | (register during demo) | — |
| Recruiter | (register during demo) | — |
| Student | (register during demo) | — |

---

## Script

### 1. Admin Setup & Overview (2 min)

**Action**: Open Tab 2 → Navigate to `/login`

> "Welcome to the AI-Powered Placement Management System. Let me start by logging in as an admin."

- Enter `admin@placement.com` / `Admin@123`
- Click **Sign In**

**Action**: Navigate to the admin panel

> "The admin dashboard shows system-wide statistics — total users, drives, job posts, and applications. This gives the admin a bird's-eye view of the entire placement lifecycle."

**Action**: Click **Users** in sidebar

> "The admin can search, filter by role, change user roles, and activate or deactivate accounts. For example, I can search for 'student' to see all student accounts."

- Type "student" in search box
- Demo role change dropdown and active toggle (explain only, don't change)

> "This is useful for managing user accounts and ensuring only active students can participate."

---

### 2. Student Registration & Profile (2 min)

**Action**: Open Tab 1 (incognito) → Navigate to `/register`

> "Now let me register as a student."

- Fill in: `John Doe`, `john@example.com`, `Student@123`, select **STUDENT**
- Click **Create Account**

> "After registration, the system auto-creates a student profile. Let me fill it out."

**Action**: Navigate to **Profile** in sidebar

> "I'll add my college details — college name, graduation year, major, and skills. This information feeds into our AI scoring engine."

- Set: `MIT`, `2027`, `Computer Science`, `3.8`, `Java, Python, SQL`
- Click **Save**

**Action**: Upload a resume

> "I can also upload my resume as a PDF. The system uses Apache Tika to parse the PDF and extract skills, experience, and education. Let me upload."

- Upload a PDF resume
- Show success toast

> "The parsed data is stored and will be used to match me with relevant job applications."

---

### 3. PO Creates a Placement Drive (1.5 min)

**Action**: Tab 2 → Register as a PO (or use a pre-registered PO)

- Logout admin, register as `po@example.com`, select **PO**, password `Po@123`
- Login as PO

> "Now let me switch to a Placement Officer role. The PO is responsible for creating placement drives."

**Action**: Click **Drives** in sidebar

> "Here I can see all drives. As a PO, I can create new placement drives."

- Click **New Drive**
- Fill in: `Summer Internship Drive 2026`, description
- Set GPA minimum `3.0`, graduation year `2027`
- Required skills: `Java, Python`
- Set deadline to a future date
- Click **Save**

> "The drive is created in DRAFT status. Let me activate it so recruiters can start posting jobs."

- Click **Activate** (green button)

> "Now the drive is active and visible to all users."

---

### 4. Recruiter Posts a Job (1.5 min)

**Action**: Tab 3 (another incognito or switch) → Register as Recruiter

- Navigate to `/register`
- Fill: `Acme Corp`, `recruiter@example.com`, `Recruiter@123`, select **RECRUITER**
- Click **Create Account**

> "Let me register as a recruiter from Acme Corp and set up the company profile."

**Action**: Go to **Profile** → fill company details

**Action**: Navigate to **Drives**

> "The recruiter can see all active drives. Let me open the one we just created."

- Click on the drive

> "Inside the drive, the recruiter can post jobs."

- Click **New Job Post**
- Fill: `Software Engineer Intern`, description, `Bangalore`, `₹50,000/mo`, `3` vacancies
- Click **Save**

> "The job post is now live with OPEN status. Students can see it and apply."

- Show the posted job in the card view

---

### 5. Student Applies & AI Scoring (1.5 min)

**Action**: Switch back to Tab 1 (student)

> "Back in the student session, let me browse drives and find our job opening."

**Action**: Navigate to **Drives** → click the drive

> "I can see the job post posted by Acme Corp."

**Action**: Click **Apply**

> "When the student applies, the system does three things:"
> 1. "Takes a snapshot of their resume for the application record"
> 2. "Runs AI scoring — comparing the student's skills against the job requirements"
> 3. "Creates a notification for both the student and the recruiter"

- Show success toast

> "Let me check the result."

**Action**: Navigate to **Applications**

> "The application shows APPLIED status. Let me now check the AI score from the recruiter side."

---

### 6. Recruiter Reviews Candidates with AI Scores (1.5 min)

**Action**: Tab 3 (recruiter) → Navigate to **Applications**

> "The recruiter can see all applications for their job posts. Let me show you the AI-powered ranked view."

- Click on the job post
- Notice the application with APPLIED status

**Action**: Click **View Rankings** or navigate to `/jobs/:id/rankings`

> "Here's the ranked candidate list. The AI has scored John's application based on skill matching — Java and Python match, giving him a score of 67 out of 100."

- Point out score, rationale, and feedback columns

> "The rationale explains the score breakdown, and the feedback tells the student what skills to improve. This helps recruiters focus on the most promising candidates first."

**Action**: Go back to applications → change status to UNDER_REVIEW

> "The recruiter can move candidates through the review pipeline: from APPLIED to UNDER_REVIEW, SHORTLISTED, and finally ACCEPTED or REJECTED."

---

### 7. Insights & Analytics (1 min)

**Action**: Stay on Tab 3 (recruiter) → Navigate to **Insights**

> "The Insights dashboard provides recruiters with actionable data:"
> - "Skill gap analysis — showing which skills are most in demand vs available"
> - "Score distribution — how applicants are ranked"
> - "Application funnel — the pipeline from applied to accepted"

**Action**: Navigate to Tab 2 (PO/admin) → Navigate to **Analytics**

> "For POs and admins, the Analytics dashboard shows:"
> - "Overview statistics — total drives, posts, placements, average AI score"
> - "Drive performance — how each drive is doing"
> - "Application funnel — overall pipeline health"

---

### 8. Notifications & Withdrawal (30 seconds)

**Action**: Tab 1 (student) → Navigate to **Notifications**

> "Students and recruiters receive real-time notifications when applications are submitted or statuses change. Let me show the student's notification for the application submission."

**Action**: Click notification → navigates to application

**Action**: Click **Withdraw** on the application

> "Students can withdraw applications before they reach a terminal status, giving them flexibility."

---

### 9. Summary (30 seconds)

> "To summarize, this system provides:"
> 1. "Full role-based access for students, recruiters, POs, and admins"
> 2. "End-to-end placement lifecycle — from drive creation to offer acceptance"
> 3. "AI-powered resume parsing and candidate scoring"
> 4. "Rich analytics and insights for data-driven decision making"
> 5. "Automated notifications keeping all stakeholders informed"

> "Thank you for watching. Questions?"

---

## Appendix: Keyboard shortcuts for smooth transitions

| Action | Shortcut |
|--------|----------|
| Switch browser tabs | `Cmd + Tab` (macOS) / `Ctrl + Tab` |
| New incognito window | `Cmd + Shift + N` |
| Open dev tools | `Cmd + Option + I` |
| Toggle sidebar (demo) | Not implemented — use mouse |

## Appendix: If something breaks

| Symptom | Fix |
|---------|-----|
| 401 on API calls | JWT expired — re-login |
| 403 on action | Wrong role — switch user |
| DB connection error | Check `docker ps` for postgres |
| Frontend blank | Check `npm run dev` terminal for errors |
