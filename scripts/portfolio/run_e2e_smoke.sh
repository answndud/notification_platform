#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
REQUEST_KEY="${REQUEST_KEY:-demo-$(date +%s)}"

if [[ "${1:-}" == "--help" ]]; then
  cat <<'EOF'
Usage: bash scripts/portfolio/run_e2e_smoke.sh

Environment variables:
  API_BASE_URL  default: http://localhost:8080
  REQUEST_KEY   default: demo-<timestamp>

Prerequisites:
  1) docker compose -f notification_platform/docker-compose.idea3.yml up -d
  2) Start API and Worker apps from notification_platform module.
EOF
  exit 0
fi

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[FAIL] required command not found: $1"
    exit 1
  fi
}

require_cmd curl
require_cmd python3

echo "== Notification Platform E2E Smoke =="
echo "Root: ${ROOT_DIR}"
echo "API base URL: ${API_BASE_URL}"
echo "Request key: ${REQUEST_KEY}"

API_CHECK_PATH="/api/v1/notifications/requests/by-key?requestKey=__e2e_precheck__"
API_CHECK_CODE="$(curl -s -o /dev/null -w "%{http_code}" "${API_BASE_URL}${API_CHECK_PATH}" || true)"
if [[ "${API_CHECK_CODE}" != "200" && "${API_CHECK_CODE}" != "404" ]]; then
  echo "[FAIL] API precheck failed (${API_BASE_URL}${API_CHECK_PATH} -> ${API_CHECK_CODE})"
  echo "Start API/Worker first, then retry."
  exit 2
fi

CREATE_PAYLOAD="$(cat <<EOF
{
  "requestKey": "${REQUEST_KEY}",
  "templateCode": "ORDER_PAID",
  "receiverIds": [1001],
  "variables": {
    "orderNo": "E2E-${REQUEST_KEY}",
    "amount": 19900
  },
  "priority": "HIGH"
}
EOF
)"

CREATE_RESPONSE="$(curl -sS -X POST "${API_BASE_URL}/api/v1/notifications/requests" \
  -H "Content-Type: application/json" \
  -d "${CREATE_PAYLOAD}")"

REQUEST_ID="$(python3 - <<'PY' "${CREATE_RESPONSE}"
import json, sys
data = json.loads(sys.argv[1])
print(data.get("data", {}).get("requestId", ""))
PY
)"

if [[ -z "${REQUEST_ID}" ]]; then
  echo "[FAIL] requestId not found in create response"
  echo "Response: ${CREATE_RESPONSE}"
  exit 3
fi

echo "[OK] create response requestId=${REQUEST_ID}"
sleep 3

TASKS_RESPONSE="$(curl -sS "${API_BASE_URL}/api/v1/notifications/tasks?requestId=${REQUEST_ID}&page=0&size=20")"
TASK_ID="$(python3 - <<'PY' "${TASKS_RESPONSE}"
import json, sys
data = json.loads(sys.argv[1])
items = data.get("data", {}).get("items", [])
print(items[0].get("taskId", "") if items else "")
PY
)"

if [[ -z "${TASK_ID}" ]]; then
  echo "[WARN] no task found yet for requestId=${REQUEST_ID}"
  echo "Tasks response: ${TASKS_RESPONSE}"
  exit 4
fi

LOGS_RESPONSE="$(curl -sS "${API_BASE_URL}/api/v1/notifications/tasks/${TASK_ID}/logs?page=0&size=20")"

echo "[OK] task found taskId=${TASK_ID}"
echo "[INFO] tasks response: ${TASKS_RESPONSE}"
echo "[INFO] task logs response: ${LOGS_RESPONSE}"
echo "[DONE] E2E smoke completed."
