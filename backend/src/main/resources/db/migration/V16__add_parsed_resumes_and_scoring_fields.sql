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

ALTER TABLE applications
    ADD COLUMN scoring_rationale TEXT,
    ADD COLUMN scoring_feedback TEXT,
    ADD COLUMN scoring_version VARCHAR(50) DEFAULT 'keyword-v1';

CREATE INDEX idx_applications_job_post_score
    ON applications(job_post_id, ai_score DESC);
