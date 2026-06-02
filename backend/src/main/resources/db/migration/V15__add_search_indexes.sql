CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(full_name);
CREATE INDEX IF NOT EXISTS idx_drives_title ON drives(title);
CREATE INDEX IF NOT EXISTS idx_job_posts_title ON job_posts(title);
CREATE INDEX IF NOT EXISTS idx_job_posts_location ON job_posts(location);
