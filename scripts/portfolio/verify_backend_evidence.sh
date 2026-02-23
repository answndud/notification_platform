#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICE_DIR="${ROOT_DIR}/notification_platform"
QUICK_MODE="false"

if [[ "${1:-}" == "--quick" ]]; then
  QUICK_MODE="true"
fi

require_file() {
  local path="$1"
  if [[ ! -f "${path}" ]]; then
    echo "[FAIL] missing file: ${path}"
    return 1
  fi
  echo "[OK] ${path}"
}

echo "== Notification Platform Backend Evidence Check =="
echo "Root: ${ROOT_DIR}"
echo "Service project: ${SERVICE_DIR}"

require_file "${SERVICE_DIR}/build.gradle"
require_file "${SERVICE_DIR}/settings.gradle"
require_file "${SERVICE_DIR}/api/src/main/java/com/example/notification/ApiApplication.java"
require_file "${SERVICE_DIR}/worker/src/main/java/com/example/notification/worker/WorkerApplication.java"
require_file "${SERVICE_DIR}/api/src/main/java/com/example/notification/domain/request/controller/NotificationRequestController.java"
require_file "${SERVICE_DIR}/worker/src/main/java/com/example/notification/worker/consumer/NotificationRequestQueuedConsumer.java"
require_file "${SERVICE_DIR}/api/src/main/resources/db/migration/V1__init_notification_request.sql"
require_file "${SERVICE_DIR}/api/src/main/resources/db/migration/V6__add_delivery_task_idempotency_constraint.sql"

if [[ "${QUICK_MODE}" == "true" ]]; then
  echo "Quick mode enabled: skipping Gradle tests."
  exit 0
fi

echo "Running service tests..."
(
  cd "${SERVICE_DIR}"
  ./gradlew :api:test :worker:test
)

echo "[DONE] Backend evidence check completed successfully."
