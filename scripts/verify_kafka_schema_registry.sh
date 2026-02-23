#!/usr/bin/env bash
set -euo pipefail

echo "[1/4] Start schema registry"
docker compose -f docker-compose.idea3.yml -f docker-compose.kafka-sr.yml up -d schema-registry

echo "[2/4] Wait for schema registry ready"
for i in {1..30}; do
  if curl -sSf http://localhost:18081/subjects >/dev/null; then
    break
  fi
  sleep 1
done
curl -sSf http://localhost:18081/subjects >/dev/null

echo "[3/4] Set BACKWARD compatibility"
curl -sSf -X PUT "http://localhost:18081/config/payment.events-value" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"compatibility":"BACKWARD"}' >/dev/null

echo "[4/4] Register v1 schema"
curl -sSf -X POST "http://localhost:18081/subjects/payment.events-value/versions" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema":"{\"type\":\"record\",\"name\":\"PaymentEvent\",\"fields\":[{\"name\":\"paymentId\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"long\"}]}"}' >/dev/null

echo "PASS: schema subject and compatibility configured"
