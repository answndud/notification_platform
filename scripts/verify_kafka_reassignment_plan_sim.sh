#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
plan = {
    "topic": "payment.events.v1",
    "partitions": {
        0: [1, 2],
        1: [2, 3],
        2: [3, 1],
    },
    "throttle_mb_per_sec": 50,
    "rollback_threshold": {
        "error_rate_pct": 5,
        "p95_latency_multiplier": 2,
    },
}

brokers = {1, 2, 3}
for _, replicas in plan["partitions"].items():
    assert len(replicas) == 2
    assert set(replicas).issubset(brokers)

assert plan["throttle_mb_per_sec"] > 0
assert plan["rollback_threshold"]["error_rate_pct"] <= 5
assert plan["rollback_threshold"]["p95_latency_multiplier"] <= 2

print("Reassignment simulation plan:", plan)
print("PASS: partition spread and rollback thresholds validated")
PY
