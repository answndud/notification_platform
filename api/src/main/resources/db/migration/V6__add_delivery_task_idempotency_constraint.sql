ALTER TABLE delivery_task
    ADD CONSTRAINT uk_delivery_task_request_receiver_channel
        UNIQUE (request_id, receiver_id, channel);
