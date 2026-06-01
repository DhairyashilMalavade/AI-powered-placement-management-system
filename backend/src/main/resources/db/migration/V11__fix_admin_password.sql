-- Fix admin password hash to actually match "Admin@123"
-- The placeholder hash in V9 was not a real BCrypt encoding of Admin@123
UPDATE users
SET password_hash = '$2a$10$UWk6dPiITCC/QRw1aqydWug/Jky4xiGlDizSmDtBb7pJ7uNtfqFRO'
WHERE email = 'admin@placement.com';
