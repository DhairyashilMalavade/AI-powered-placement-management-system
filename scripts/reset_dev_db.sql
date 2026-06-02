-- =============================================================================
-- Development Database Reset Script
-- =============================================================================
-- Preserves: admin@placement.com, ADMIN role, admin PO profile,
--            flyway_schema_history, all migrations
-- Deletes:   everything else — students, recruiters, POs (except admin),
--            drives, job posts, applications, notifications, uploaded files
-- =============================================================================
-- Usage:
--   1. (Optional) Backup: pg_dump -U postgres placement_db > backup_$(date +%Y%m%d_%H%M%S).sql
--   2. psql -U postgres -d placement_db -f scripts/reset_dev_db.sql
--   3. Clean up resume files from ./uploads/ (paths printed below)
-- =============================================================================

DO $$
DECLARE
    admin_uuid UUID;
    deleted_students INT;
    deleted_recruiters INT;
    deleted_pos INT;
    deleted_drives INT;
    deleted_jobposts INT;
    deleted_apps INT;
    deleted_notifs INT;
    resume_paths TEXT[];
    path TEXT;
BEGIN
    -- Identify the admin user to preserve
    SELECT id INTO admin_uuid FROM users WHERE email = 'admin@placement.com';
    IF admin_uuid IS NULL THEN
        RAISE EXCEPTION 'admin@placement.com not found — aborting';
    END IF;
    RAISE NOTICE 'Preserving admin user: %', admin_uuid;

    -- === 1. COLLECT RESUME FILE PATHS (before deletion) ===
    SELECT ARRAY_AGG(COALESCE(resume_file_path, ''))
        INTO resume_paths
        FROM student_profiles
        WHERE resume_file_path IS NOT NULL AND resume_file_path != '';
    RAISE NOTICE 'Resume files to clean up: %', array_length(resume_paths, 1);

    -- === 2. COUNT BEFORE CASCADE DELETES ===
    SELECT COUNT(*) INTO deleted_notifs FROM notifications;
    SELECT COUNT(*) INTO deleted_drives FROM drives;
    SELECT COUNT(*) INTO deleted_jobposts FROM job_posts;
    SELECT COUNT(*) INTO deleted_apps FROM applications;

    -- === 3. DELETE NOTIFICATIONS (ON DELETE CASCADE on user_id, but explicit for clarity) ===
    DELETE FROM notifications;

    -- === 4. DELETE DRIVES (cascades to job_posts → applications) ===
    DELETE FROM drives;
    RAISE NOTICE 'Deleted notifications: %, drives: %, job posts: %, applications: %',
        deleted_notifs, deleted_drives, deleted_jobposts, deleted_apps;

    -- === 5. DELETE NON-ADMIN USERS (cascades to profiles, leftover notifications) ===
    SELECT COUNT(*) INTO deleted_students FROM users WHERE role = 'STUDENT' AND id != admin_uuid;
    SELECT COUNT(*) INTO deleted_recruiters FROM users WHERE role = 'RECRUITER' AND id != admin_uuid;
    SELECT COUNT(*) INTO deleted_pos FROM users WHERE role = 'PO' AND id != admin_uuid;

    DELETE FROM users WHERE id != admin_uuid;
    RAISE NOTICE 'Deleted users — STUDENT: %, RECRUITER: %, PO: %', deleted_students, deleted_recruiters, deleted_pos;

    -- === 6. VERIFY ===
    IF EXISTS (SELECT 1 FROM users WHERE email = 'admin@placement.com' AND role = 'ADMIN' AND is_active = true) THEN
        RAISE NOTICE 'Admin user preserved and active: OK';
    ELSE
        RAISE WARNING 'Admin user check FAILED';
    END IF;

    IF EXISTS (SELECT 1 FROM placement_officer_profiles WHERE user_id = admin_uuid) THEN
        RAISE NOTICE 'Admin PO profile preserved: OK';
    ELSE
        RAISE WARNING 'Admin PO profile check FAILED';
    END IF;

    -- === 7. PRINT FILE CLEANUP INSTRUCTIONS ===
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'FILE CLEANUP';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Resume files to delete from ./uploads/:';
    IF resume_paths IS NOT NULL THEN
        FOREACH path IN ARRAY resume_paths
        LOOP
            RAISE NOTICE '  rm -f ./uploads/%', path;
        END LOOP;
    END IF;
    RAISE NOTICE 'Or run: rm -f ./uploads/*.pdf';
    RAISE NOTICE '(In Docker: docker compose exec backend rm -f /app/uploads/*.pdf)';

    -- === 8. SUMMARY ===
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'RESET SUMMARY';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Users deleted:            %', deleted_students + deleted_recruiters + deleted_pos;
    RAISE NOTICE '  - STUDENT:              %', deleted_students;
    RAISE NOTICE '  - RECRUITER:            %', deleted_recruiters;
    RAISE NOTICE '  - PO:                   %', deleted_pos;
    RAISE NOTICE 'Drives deleted:           %', deleted_drives;
    RAISE NOTICE 'Job posts deleted:        %', deleted_jobposts;
    RAISE NOTICE 'Applications deleted:     %', deleted_apps;
    RAISE NOTICE 'Notifications deleted:    %', deleted_notifs;
    RAISE NOTICE 'Resume files to remove:   %', COALESCE(array_length(resume_paths, 1), 0);
    RAISE NOTICE 'Admin user preserved:     admin@placement.com';
    RAISE NOTICE '============================================================';
END $$;
