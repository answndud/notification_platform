#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICE_DIR="${ROOT_DIR}/notification_platform"

echo "== API Surface Regression Tests (task/dlq/log endpoints) =="
echo "Service project: ${SERVICE_DIR}"

(
  cd "${SERVICE_DIR}"
  ./gradlew :api:test \
    --tests "*DeliveryTaskControllerTest" \
    --tests "*DeliveryTaskServiceTest" \
    --tests "*DlqControllerTest" \
    --tests "*DlqServiceTest"
)

echo "[DONE] API surface regression tests passed."
