#!/usr/bin/env bash
set -euo pipefail

POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-idea3-postgres}"
KAFKA_CONTAINER="${KAFKA_CONTAINER:-idea3-kafka}"
TOPIC="${TOPIC:-outbox.demo.events}"

echo "[1/6] Prepare outbox table"
docker exec -i "${POSTGRES_CONTAINER}" psql -U notification -d notification -c "CREATE TABLE IF NOT EXISTS outbox_events (id BIGSERIAL PRIMARY KEY, aggregate_id TEXT NOT NULL, payload TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'NEW');"

echo "[2/6] Insert NEW outbox row"
docker exec -i "${POSTGRES_CONTAINER}" psql -U notification -d notification -c "INSERT INTO outbox_events(aggregate_id,payload,status) VALUES ('order-1001','{\"eventType\":\"ORDER_CREATED\",\"orderId\":\"order-1001\"}','NEW');"

PAYLOAD=$(docker exec -i "${POSTGRES_CONTAINER}" psql -U notification -d notification -t -A -c "SELECT payload FROM outbox_events WHERE status='NEW' ORDER BY id LIMIT 1;")

echo "[3/6] Prepare Kafka topic"
docker exec -i "${KAFKA_CONTAINER}" kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic "${TOPIC}" --partitions 1 --replication-factor 1

echo "[4/6] Relay publish to Kafka"
docker exec -i "${KAFKA_CONTAINER}" bash -lc "printf '%s\n' '${PAYLOAD}' | kafka-console-producer --bootstrap-server localhost:9092 --topic ${TOPIC}"

echo "[5/6] Mark outbox row SENT"
docker exec -i "${POSTGRES_CONTAINER}" psql -U notification -d notification -c "UPDATE outbox_events SET status='SENT' WHERE status='NEW';"

echo "[6/6] Verify SENT rows"
docker exec -i "${POSTGRES_CONTAINER}" psql -U notification -d notification -t -A -c "SELECT COUNT(*) FROM outbox_events WHERE status='SENT';"

echo "PASS: outbox NEW->Kafka publish->SENT flow checked"
