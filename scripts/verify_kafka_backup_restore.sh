#!/usr/bin/env bash
set -euo pipefail

KAFKA_CONTAINER="${KAFKA_CONTAINER:-idea3-kafka}"
SRC_TOPIC="${SRC_TOPIC:-backup.demo.events}"
DST_TOPIC="${DST_TOPIC:-backup.demo.events.restore}"

echo "[1/5] Prepare topics"
docker exec -i "${KAFKA_CONTAINER}" kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic "${SRC_TOPIC}" --partitions 1 --replication-factor 1
docker exec -i "${KAFKA_CONTAINER}" kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic "${DST_TOPIC}" --partitions 1 --replication-factor 1

echo "[2/5] Seed source topic"
docker exec -i "${KAFKA_CONTAINER}" bash -lc "printf 'backup-1\nbackup-2\nbackup-3\n' | kafka-console-producer --bootstrap-server localhost:9092 --topic ${SRC_TOPIC}"

echo "[3/5] Backup messages to file"
docker exec -i "${KAFKA_CONTAINER}" bash -lc "kafka-console-consumer --bootstrap-server localhost:9092 --topic ${SRC_TOPIC} --from-beginning --max-messages 3 --timeout-ms 5000 > /tmp/kafka_backup_dump.txt"

echo "[4/5] Restore messages to destination topic"
docker exec -i "${KAFKA_CONTAINER}" bash -lc "kafka-console-producer --bootstrap-server localhost:9092 --topic ${DST_TOPIC} < /tmp/kafka_backup_dump.txt"

echo "[5/5] Verify destination offsets"
docker exec -i "${KAFKA_CONTAINER}" kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic "${DST_TOPIC}" --time -1

echo "PASS: backup dump and restore replay flow completed"
