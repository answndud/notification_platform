#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
from math import ceil

def gib(n):
    return n / (1024**3)

scenarios = [
    {"name": "payments", "daily_msgs": 5_000_000, "avg_bytes": 1200, "retention_days": 7, "rf": 3, "headroom": 0.3},
    {"name": "notifications", "daily_msgs": 20_000_000, "avg_bytes": 300, "retention_days": 3, "rf": 2, "headroom": 0.2},
]

print("Kafka capacity sizing verification")
for s in scenarios:
    raw = s["daily_msgs"] * s["avg_bytes"] * s["retention_days"] * s["rf"]
    total = int(raw * (1 + s["headroom"]))
    print(f"- {s['name']}: raw={gib(raw):.2f} GiB, with headroom={gib(total):.2f} GiB")

print("PASS: formula + headroom calculation completed")
PY
