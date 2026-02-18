#!/usr/bin/env bash

set -euo pipefail

METRICS_URL="${1:-http://localhost:8080/api/v1/notifications/metrics}"
METRICS_PAYLOAD="${METRICS_PAYLOAD:-}"
LAG_WARN_THRESHOLD="${LAG_WARN_THRESHOLD:-100}"
LAG_CRITICAL_THRESHOLD="${LAG_CRITICAL_THRESHOLD:-1000}"
FAILED_WARN_THRESHOLD="${FAILED_WARN_THRESHOLD:-200}"
DLQ_WARN_THRESHOLD="${DLQ_WARN_THRESHOLD:-100}"
EXIT_ON_ALERT="${EXIT_ON_ALERT:-false}"

if [ -n "$METRICS_PAYLOAD" ]; then
  payload="$METRICS_PAYLOAD"
else
  payload="$(curl -fsS "$METRICS_URL")"
fi

export METRICS_PAYLOAD_JSON="$payload"
export METRICS_URL
export LAG_WARN_THRESHOLD
export LAG_CRITICAL_THRESHOLD
export FAILED_WARN_THRESHOLD
export DLQ_WARN_THRESHOLD

alert_json="$(python3 - <<'PY'
import json
import os
import sys


def parse_int(value, name):
    try:
        return int(value)
    except (TypeError, ValueError):
        raise ValueError(f"{name} must be an integer")


try:
    payload = json.loads(os.environ["METRICS_PAYLOAD_JSON"])
    data = payload["data"]

    request_queued_lag = parse_int(data.get("requestQueuedLag"), "data.requestQueuedLag")
    failed_tasks = parse_int(data.get("failedTasks"), "data.failedTasks")
    dlq_tasks = parse_int(data.get("dlqTasks"), "data.dlqTasks")

    lag_warn_threshold = parse_int(os.environ["LAG_WARN_THRESHOLD"], "LAG_WARN_THRESHOLD")
    lag_critical_threshold = parse_int(os.environ["LAG_CRITICAL_THRESHOLD"], "LAG_CRITICAL_THRESHOLD")
    failed_warn_threshold = parse_int(os.environ["FAILED_WARN_THRESHOLD"], "FAILED_WARN_THRESHOLD")
    dlq_warn_threshold = parse_int(os.environ["DLQ_WARN_THRESHOLD"], "DLQ_WARN_THRESHOLD")

    severity = "OK"
    reason = "within_threshold"

    if request_queued_lag == -1:
        severity = "CRITICAL"
        reason = "lag_unavailable"
    elif request_queued_lag > lag_critical_threshold:
        severity = "CRITICAL"
        reason = f"lag_gt_{lag_critical_threshold}"
    elif request_queued_lag > lag_warn_threshold:
        severity = "WARNING"
        reason = f"lag_gt_{lag_warn_threshold}"

    if (failed_tasks > failed_warn_threshold or dlq_tasks > dlq_warn_threshold) and severity == "OK":
        severity = "WARNING"
        reason = "failed_or_dlq_growth"

    print(json.dumps({
        "severity": severity,
        "reason": reason,
        "requestQueuedLag": request_queued_lag,
        "failedTasks": failed_tasks,
        "dlqTasks": dlq_tasks,
        "metricsUrl": os.environ["METRICS_URL"],
        "thresholds": {
            "lagWarning": lag_warn_threshold,
            "lagCritical": lag_critical_threshold,
            "failedWarning": failed_warn_threshold,
            "dlqWarning": dlq_warn_threshold,
        },
    }, ensure_ascii=True, indent=2))
except (KeyError, ValueError, json.JSONDecodeError) as exc:
    print(f"Invalid metrics payload: {exc}", file=sys.stderr)
    sys.exit(1)
PY
)"

printf "%s\n" "$alert_json"

severity="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["severity"])' <<< "$alert_json")"

if [ "$EXIT_ON_ALERT" = "true" ] && [ "$severity" != "OK" ]; then
  exit 2
fi
