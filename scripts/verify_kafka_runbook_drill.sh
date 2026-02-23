#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
timeline = {
    "detect_min": 0,
    "first_update_min": 3,
    "mitigation_plan_min": 9,
    "recovery_min": 28,
}

assert timeline["first_update_min"] <= 3, "first update SLA failed"
assert timeline["mitigation_plan_min"] <= 10, "mitigation SLA failed"
assert timeline["recovery_min"] <= 30, "recovery target failed"

print("Runbook drill timeline:", timeline)
print("PASS: drill SLAs satisfy runbook targets")
PY
