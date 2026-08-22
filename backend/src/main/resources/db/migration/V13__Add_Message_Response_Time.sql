-- Nullable: only AI messages have a response time, and only ones generated
-- after this column existed. NULL means "not measured", not "zero latency".
ALTER TABLE messages
    ADD COLUMN response_time_ms INTEGER;
