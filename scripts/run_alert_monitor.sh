#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ "$#" -ge 1 ]; then
  METRICS_URL="$1"
else
  METRICS_URL="${NOTIFICATION_METRICS_URL:-http://localhost:8080/api/v1/notifications/metrics}"
fi
ALERT_TEST_PAYLOAD="${ALERT_TEST_PAYLOAD:-}"

if [ -z "$METRICS_URL" ] && [ -z "$ALERT_TEST_PAYLOAD" ]; then
  echo "[monitor] skipped: metrics URL and test payload are both empty"
  exit 0
fi

if [ -n "$ALERT_TEST_PAYLOAD" ]; then
  payload_type="$(ALERT_TEST_PAYLOAD="$ALERT_TEST_PAYLOAD" python3 - <<'PY'
import json
import os
import sys

try:
    payload = json.loads(os.environ["ALERT_TEST_PAYLOAD"])
except json.JSONDecodeError as exc:
    print(f"Invalid ALERT_TEST_PAYLOAD JSON: {exc}", file=sys.stderr)
    sys.exit(1)

if isinstance(payload, dict) and isinstance(payload.get("data"), dict):
    print("metrics")
elif isinstance(payload, dict):
    print("alert")
else:
    print("Invalid ALERT_TEST_PAYLOAD JSON: root must be object", file=sys.stderr)
    sys.exit(1)
PY
)"

  if [ "$payload_type" = "metrics" ]; then
    alert_json="$(METRICS_PAYLOAD="$ALERT_TEST_PAYLOAD" "$SCRIPT_DIR/check_metrics_alerts.sh" "$METRICS_URL")"
  else
    alert_json="$ALERT_TEST_PAYLOAD"
  fi
else
  alert_json="$($SCRIPT_DIR/check_metrics_alerts.sh "$METRICS_URL")"
fi

echo "$alert_json"

printf "%s" "$alert_json" | "$SCRIPT_DIR/send_alert.sh"
