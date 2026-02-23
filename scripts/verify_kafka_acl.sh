#!/usr/bin/env bash
set -euo pipefail

BROKER_CONTAINER="${BROKER_CONTAINER:-idea3-kafka-sec}"
BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-localhost:19092}"
TOPIC="${TOPIC:-acl.demo.events}"
PRINCIPAL="${PRINCIPAL:-User:payment-service}"

echo "[1/5] Topic prepare: ${TOPIC}"
docker exec -i "${BROKER_CONTAINER}" kafka-topics \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --create --if-not-exists \
  --topic "${TOPIC}" --partitions 1 --replication-factor 1

echo "[2/5] ACL add for ${PRINCIPAL}"
docker exec -i "${BROKER_CONTAINER}" kafka-acls \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --add --allow-principal "${PRINCIPAL}" \
  --operation Read --operation Write --topic "${TOPIC}"

echo "[3/5] ACL list"
docker exec -i "${BROKER_CONTAINER}" kafka-acls \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --list --topic "${TOPIC}"

echo "[4/5] ACL remove"
docker exec -i "${BROKER_CONTAINER}" kafka-acls \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --remove --allow-principal "${PRINCIPAL}" \
  --operation Read --operation Write --topic "${TOPIC}" --force

echo "[5/5] ACL list after remove (expect empty)"
docker exec -i "${BROKER_CONTAINER}" kafka-acls \
  --bootstrap-server "${BOOTSTRAP_SERVER}" \
  --list --topic "${TOPIC}"
