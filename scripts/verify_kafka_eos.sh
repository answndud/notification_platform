#!/usr/bin/env bash
set -euo pipefail

BROKER_CONTAINER="${BROKER_CONTAINER:-idea3-kafka}"
BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-localhost:9092}"
TOPIC="${TOPIC:-eos.tx.demo}"
NUM_RECORDS="${NUM_RECORDS:-30}"

echo "[1/4] Topic prepare: ${TOPIC}"
docker exec -i "${BROKER_CONTAINER}" kafka-topics \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --create --if-not-exists \
  --topic "${TOPIC}" --partitions 1 --replication-factor 1

echo "[2/4] Transactional producer-perf-test run"
docker exec -i "${BROKER_CONTAINER}" kafka-producer-perf-test \
  --topic "${TOPIC}" \
  --num-records "${NUM_RECORDS}" \
  --throughput -1 \
  --record-size 20 \
  --transaction-duration-ms 1000 \
  --producer-props bootstrap.servers="${BOOTSTRAP_SERVER}" acks=all enable.idempotence=true

echo "[3/4] read_uncommitted sample"
docker exec -i "${BROKER_CONTAINER}" kafka-console-consumer \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --topic "${TOPIC}" --from-beginning --max-messages 5 \
  --consumer-property isolation.level=read_uncommitted \
  --property print.offset=true

echo "[4/4] read_committed sample"
docker exec -i "${BROKER_CONTAINER}" kafka-console-consumer \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --topic "${TOPIC}" --from-beginning --max-messages 5 \
  --consumer-property isolation.level=read_committed \
  --property print.offset=true

echo "Done: compare offsets/records across isolation levels."
