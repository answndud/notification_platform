ALTER TABLE delivery_task
    ADD COLUMN force_fail BOOLEAN NOT NULL DEFAULT false;
