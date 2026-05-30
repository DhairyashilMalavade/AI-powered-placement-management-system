CREATE TABLE drives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    min_gpa DECIMAL(3,2),
    allowed_graduation_years INTEGER[],
    required_skills TEXT[],
    additional_criteria TEXT,
    application_deadline TIMESTAMP NOT NULL,
    drive_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'COMPLETED')),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
