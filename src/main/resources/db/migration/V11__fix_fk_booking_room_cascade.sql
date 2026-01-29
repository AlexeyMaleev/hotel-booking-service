-- V11__fix_fk_booking_room_cascade.sql

ALTER TABLE bookings
DROP CONSTRAINT fk_booking_room;

ALTER TABLE bookings
ADD CONSTRAINT fk_booking_room
FOREIGN KEY (room_id)
REFERENCES rooms(id)
ON DELETE CASCADE;