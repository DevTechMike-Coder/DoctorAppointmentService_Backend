-- Video-call room per appointment. Created lazily on first join; NULL until then.
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS meeting_room_name VARCHAR(64),
    ADD COLUMN IF NOT EXISTS meeting_room_url  VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_appointments_meeting_room_name
    ON appointments (meeting_room_name) WHERE meeting_room_name IS NOT NULL;
