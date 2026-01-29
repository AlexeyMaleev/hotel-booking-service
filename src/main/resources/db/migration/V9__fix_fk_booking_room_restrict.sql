-- V9__fix_fk_booking_room_restrict.sql
-- When testing found that with ON DELETE NO ACTION postgres deletes the parent record
-- Fix for correct it

ALTER TABLE bookings
DROP CONSTRAINT fk_booking_room;

ALTER TABLE bookings
ADD CONSTRAINT fk_booking_room
FOREIGN KEY (room_id)
REFERENCES rooms(id)
ON DELETE RESTRICT;