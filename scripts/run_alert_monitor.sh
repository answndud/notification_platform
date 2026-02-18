#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
METRICS_URL="${1:-${NOTIFICATION_METRICS_URL:-http://localhost:8080/api/v1/notifications/metrics}}"
ALERT_TEST_PAYLOAD="${ALERT_TEST_PAYLOAD:-}"

if [ -n "$ALERT_TEST_PAYLOAD" ]; then
  alert_json="$ALERT_TEST_PAYLOAD"
else
  alert_json="$($SCRIPT_DIR/check_metrics_alerts.sh "$METRICS_URL")"
fi

echo "$alert_json"

printf "%s" "$alert_json" | "$SCRIPT_DIR/send_alert.sh"
