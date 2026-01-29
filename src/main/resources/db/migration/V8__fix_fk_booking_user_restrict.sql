-- V8__fix_fk_booking_user_restrict.sql
-- When testing found that with ON DELETE NO ACTION postgres deletes the parent record
-- Fix for correct it

ALTER TABLE bookings
DROP CONSTRAINT fk_booking_user;

ALTER TABLE bookings
ADD CONSTRAINT fk_booking_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE RESTRICT;