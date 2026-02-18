ALTER TABLE delivery_task
    ADD COLUMN backoff_base_sec INT NOT NULL DEFAULT 2;
