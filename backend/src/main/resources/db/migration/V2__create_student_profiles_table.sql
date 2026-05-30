CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    college_name VARCHAR(255) NOT NULL,
    graduation_year INTEGER NOT NULL,
    major VARCHAR(255) NOT NULL,
    gpa DECIMAL(3,2),
    skills TEXT[],
    resume_file_path VARCHAR(500),
    phone VARCHAR(20)
);
