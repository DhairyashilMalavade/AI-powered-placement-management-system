-- Password: Admin@123 (BCrypt hash)
-- Generate via: https://bcrypt-generator.com/ or Spring Boot's BCryptPasswordEncoder
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
DO $$
DECLARE
    admin_id UUID;
BEGIN
    INSERT INTO users (id, email, password_hash, full_name, role, is_active)
    VALUES (
        gen_random_uuid(),
        'admin@placement.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'System Admin',
        'ADMIN',
        true
    )
    RETURNING id INTO admin_id;

    INSERT INTO placement_officer_profiles (id, user_id, college_name, department)
    VALUES (gen_random_uuid(), admin_id, 'Placement College', 'Administration');
END $$;
