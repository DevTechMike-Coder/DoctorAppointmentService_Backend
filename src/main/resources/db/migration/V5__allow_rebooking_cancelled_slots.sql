-- A cancelled appointment stays in the table as history and its slot is released (is_booked = false)
-- so it can be booked again. The old UNIQUE (slot_id) constraint made every rebooking of a cancelled
-- slot fail with a duplicate-key error. Enforce "at most one ACTIVE appointment per slot" instead.
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS uq_appointments_slot_id;

CREATE UNIQUE INDEX IF NOT EXISTS uq_appointments_active_slot
    ON appointments (slot_id) WHERE status <> 'CANCELLED';

-- The dropped constraint also provided an index on slot_id; keep FK lookups fast.
CREATE INDEX IF NOT EXISTS idx_appointments_slot_id ON appointments (slot_id);
