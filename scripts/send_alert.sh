#!/usr/bin/env bash

set -euo pipefail

INPUT_FILE="${1:-}"
ALERT_DRY_RUN="${ALERT_DRY_RUN:-true}"
SLACK_WEBHOOK_URL="${NOTIFICATION_ALERT_SLACK_WEBHOOK:-}"
PAGERDUTY_ROUTING_KEY="${NOTIFICATION_ALERT_PAGERDUTY_KEY:-}"

if [ -n "$INPUT_FILE" ]; then
  if [ ! -f "$INPUT_FILE" ]; then
    echo "Input file not found: $INPUT_FILE" >&2
    exit 1
  fi
  alert_json="$(cat "$INPUT_FILE")"
else
  alert_json="$(cat)"
fi

if [ -z "$alert_json" ]; then
  echo "Empty alert payload" >&2
  exit 1
fi

export ALERT_JSON="$alert_json"
alert_meta="$(python3 - <<'PY'
import json
import os
import sys
from datetime import datetime, timezone

try:
    data = json.loads(os.environ["ALERT_JSON"])
except (KeyError, json.JSONDecodeError) as exc:
    print(f"Invalid alert payload: {exc}", file=sys.stderr)
    sys.exit(1)

severity = data.get("severity")
if severity not in {"OK", "WARNING", "CRITICAL"}:
    print("Invalid alert payload: severity must be one of OK, WARNING, CRITICAL", file=sys.stderr)
    sys.exit(1)

lines = [
    "[Notification Platform Alert]",
    f"severity={severity}",
    f"reason={data.get('reason')}",
    f"requestQueuedLag={data.get('requestQueuedLag')}",
    f"failedTasks={data.get('failedTasks')}",
    f"dlqTasks={data.get('dlqTasks')}",
    f"metricsUrl={data.get('metricsUrl')}",
    f"timestamp={datetime.now(timezone.utc).isoformat()}",
]

print(json.dumps({"severity": severity, "summary": " | ".join(lines)}))
PY
)"

severity="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["severity"])' <<< "$alert_meta")"

if [ "$severity" = "OK" ]; then
  echo "No alert to send (severity=OK)."
  exit 0
fi

summary_text="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["summary"])' <<< "$alert_meta")"

if [ "$ALERT_DRY_RUN" = "true" ]; then
  echo "[DRY_RUN] Alert payload"
  echo "$summary_text"
  exit 0
fi

if [ -n "$SLACK_WEBHOOK_URL" ]; then
  export ALERT_TEXT="$summary_text"
  slack_payload="$(python3 - <<'PY'
import json
import os
print(json.dumps({"text": os.environ["ALERT_TEXT"]}))
PY
)"
  curl -fsS -X POST "$SLACK_WEBHOOK_URL" \
    -H "Content-Type: application/json" \
    -d "$slack_payload" >/dev/null
fi

if [ "$severity" = "CRITICAL" ] && [ -n "$PAGERDUTY_ROUTING_KEY" ]; then
  export ALERT_TEXT="$summary_text"
  export PD_KEY="$PAGERDUTY_ROUTING_KEY"
  pagerduty_payload="$(python3 - <<'PY'
import json
import os
print(json.dumps({
    "routing_key": os.environ["PD_KEY"],
    "event_action": "trigger",
    "payload": {
        "summary": os.environ["ALERT_TEXT"],
        "severity": "critical",
        "source": "notification-platform"
    }
}))
PY
)"
  curl -fsS -X POST "https://events.pagerduty.com/v2/enqueue" \
    -H "Content-Type: application/json" \
    -d "$pagerduty_payload" >/dev/null
fi

echo "Alert sent. severity=$severity"
