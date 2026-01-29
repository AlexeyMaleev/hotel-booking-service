-- V10__fix_fk_booking_user_cascade.sql

ALTER TABLE bookings
DROP CONSTRAINT fk_booking_user;

ALTER TABLE bookings
ADD CONSTRAINT fk_booking_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;