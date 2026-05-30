CREATE INDEX idx_applications_student_job_status ON applications(student_id, job_post_id, status);
CREATE INDEX idx_applications_job_status ON applications(job_post_id, status);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE is_read = false;
CREATE INDEX idx_drives_status ON drives(status);
CREATE INDEX idx_drives_created_by ON drives(created_by);
CREATE INDEX idx_job_posts_drive ON job_posts(drive_id);
