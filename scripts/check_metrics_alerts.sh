#!/usr/bin/env bash

set -euo pipefail

METRICS_URL="${1:-http://localhost:8080/api/v1/notifications/metrics}"
METRICS_PAYLOAD="${METRICS_PAYLOAD:-}"
LAG_WARN_THRESHOLD="${LAG_WARN_THRESHOLD:-100}"
LAG_CRITICAL_THRESHOLD="${LAG_CRITICAL_THRESHOLD:-1000}"
MALFORMED_LAG_WARN_THRESHOLD="${MALFORMED_LAG_WARN_THRESHOLD:-20}"
MALFORMED_LAG_CRITICAL_THRESHOLD="${MALFORMED_LAG_CRITICAL_THRESHOLD:-100}"
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
export MALFORMED_LAG_WARN_THRESHOLD
export MALFORMED_LAG_CRITICAL_THRESHOLD
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


def validate_non_negative(value, name):
    if value < 0:
        raise ValueError(f"{name} must be >= 0")


def validate_threshold_order(warn_value, critical_value, warn_name, critical_name):
    if warn_value >= critical_value:
        raise ValueError(f"{warn_name} must be less than {critical_name}")


try:
    payload = json.loads(os.environ["METRICS_PAYLOAD_JSON"])
    data = payload["data"]

    request_queued_lag = parse_int(data.get("requestQueuedLag"), "data.requestQueuedLag")
    malformed_queued_lag = parse_int(data.get("malformedQueuedLag", 0), "data.malformedQueuedLag")
    failed_tasks = parse_int(data.get("failedTasks"), "data.failedTasks")
    dlq_tasks = parse_int(data.get("dlqTasks"), "data.dlqTasks")

    lag_warn_threshold = parse_int(os.environ["LAG_WARN_THRESHOLD"], "LAG_WARN_THRESHOLD")
    lag_critical_threshold = parse_int(os.environ["LAG_CRITICAL_THRESHOLD"], "LAG_CRITICAL_THRESHOLD")
    malformed_lag_warn_threshold = parse_int(
        os.environ["MALFORMED_LAG_WARN_THRESHOLD"], "MALFORMED_LAG_WARN_THRESHOLD"
    )
    malformed_lag_critical_threshold = parse_int(
        os.environ["MALFORMED_LAG_CRITICAL_THRESHOLD"], "MALFORMED_LAG_CRITICAL_THRESHOLD"
    )
    failed_warn_threshold = parse_int(os.environ["FAILED_WARN_THRESHOLD"], "FAILED_WARN_THRESHOLD")
    dlq_warn_threshold = parse_int(os.environ["DLQ_WARN_THRESHOLD"], "DLQ_WARN_THRESHOLD")

    validate_non_negative(lag_warn_threshold, "LAG_WARN_THRESHOLD")
    validate_non_negative(lag_critical_threshold, "LAG_CRITICAL_THRESHOLD")
    validate_non_negative(malformed_lag_warn_threshold, "MALFORMED_LAG_WARN_THRESHOLD")
    validate_non_negative(malformed_lag_critical_threshold, "MALFORMED_LAG_CRITICAL_THRESHOLD")
    validate_non_negative(failed_warn_threshold, "FAILED_WARN_THRESHOLD")
    validate_non_negative(dlq_warn_threshold, "DLQ_WARN_THRESHOLD")

    validate_threshold_order(
        lag_warn_threshold,
        lag_critical_threshold,
        "LAG_WARN_THRESHOLD",
        "LAG_CRITICAL_THRESHOLD",
    )
    validate_threshold_order(
        malformed_lag_warn_threshold,
        malformed_lag_critical_threshold,
        "MALFORMED_LAG_WARN_THRESHOLD",
        "MALFORMED_LAG_CRITICAL_THRESHOLD",
    )

    severity_rank = {"OK": 0, "WARNING": 1, "CRITICAL": 2}
    state = {"severity": "OK"}
    reason_details = []

    def apply_reason(next_severity, next_reason):
        if severity_rank[next_severity] > severity_rank[state["severity"]]:
            state["severity"] = next_severity
        reason_details.append(next_reason)

    if request_queued_lag == -1:
        apply_reason("CRITICAL", "lag_unavailable")
    elif request_queued_lag > lag_critical_threshold:
        apply_reason("CRITICAL", f"lag_gt_{lag_critical_threshold}")
    elif request_queued_lag > lag_warn_threshold:
        apply_reason("WARNING", f"lag_gt_{lag_warn_threshold}")

    if malformed_queued_lag == -1:
        apply_reason("WARNING", "malformed_lag_unavailable")
    elif malformed_queued_lag > malformed_lag_critical_threshold:
        apply_reason("CRITICAL", f"malformed_lag_gt_{malformed_lag_critical_threshold}")
    elif malformed_queued_lag > malformed_lag_warn_threshold:
        apply_reason("WARNING", f"malformed_lag_gt_{malformed_lag_warn_threshold}")

    if failed_tasks > failed_warn_threshold:
        apply_reason("WARNING", f"failed_gt_{failed_warn_threshold}")
    if dlq_tasks > dlq_warn_threshold:
        apply_reason("WARNING", f"dlq_gt_{dlq_warn_threshold}")

    if not reason_details:
        reason_details.append("within_threshold")

    severity = state["severity"]
    reason = reason_details[0]

    print(json.dumps({
        "severity": severity,
        "reason": reason,
        "reasonDetails": reason_details,
        "requestQueuedLag": request_queued_lag,
        "malformedQueuedLag": malformed_queued_lag,
        "failedTasks": failed_tasks,
        "dlqTasks": dlq_tasks,
        "metricsUrl": os.environ["METRICS_URL"],
        "thresholds": {
            "lagWarning": lag_warn_threshold,
            "lagCritical": lag_critical_threshold,
            "malformedLagWarning": malformed_lag_warn_threshold,
            "malformedLagCritical": malformed_lag_critical_threshold,
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
