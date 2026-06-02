CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_student_profiles_user_id ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_recruiter_profiles_user_id ON recruiter_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_po_profiles_user_id ON placement_officer_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_job_posts_recruiter ON job_posts(recruiter_id);
CREATE INDEX IF NOT EXISTS idx_applications_student ON applications(student_id);
