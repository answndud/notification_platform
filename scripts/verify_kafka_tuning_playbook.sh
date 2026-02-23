#!/usr/bin/env bash
set -euo pipefail

KAFKA_CONTAINER="${KAFKA_CONTAINER:-idea3-kafka}"
TOPIC="${TOPIC:-tuning.demo.events}"

echo "[1/3] Prepare topic"
docker exec -i "${KAFKA_CONTAINER}" kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic "${TOPIC}" --partitions 1 --replication-factor 1

echo "[2/3] Baseline run"
docker exec -i "${KAFKA_CONTAINER}" kafka-producer-perf-test \
  --topic "${TOPIC}" --num-records 200 --throughput -1 --record-size 100 \
  --producer-props bootstrap.servers=localhost:9092 acks=1 >/tmp/kafka_tuning_baseline.log

echo "[3/3] Tuned run"
docker exec -i "${KAFKA_CONTAINER}" kafka-producer-perf-test \
  --topic "${TOPIC}" --num-records 200 --throughput -1 --record-size 100 \
  --producer-props bootstrap.servers=localhost:9092 acks=all linger.ms=5 batch.size=32768 compression.type=lz4 >/tmp/kafka_tuning_tuned.log

echo "PASS: baseline and tuned perf runs completed"
