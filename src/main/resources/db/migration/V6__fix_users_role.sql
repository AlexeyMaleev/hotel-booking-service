-- V6__fix_users_role.sql
ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(50)
    USING role::text;

DROP TYPE IF EXISTS user_role;