#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
before_after = {
    "api_p95_ms": (1200, 430),
    "consumer_lag": (18000, 2300),
    "reprocess_success_rate": (62, 96),
    "detect_to_mitigate_min": (22, 7),
}

assert before_after["api_p95_ms"][1] < before_after["api_p95_ms"][0]
assert before_after["consumer_lag"][1] < before_after["consumer_lag"][0]
assert before_after["reprocess_success_rate"][1] > before_after["reprocess_success_rate"][0]
assert before_after["detect_to_mitigate_min"][1] < before_after["detect_to_mitigate_min"][0]

print("Case study metric comparison:", before_after)
print("PASS: improvement direction checks passed")
PY
