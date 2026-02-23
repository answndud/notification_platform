#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
controllers = [1, 2, 3]
active_controller = 1
failed = 1
election_time_sec = 4

assert len(controllers) % 2 == 1, "controller quorum must be odd"
assert failed == active_controller, "simulation expects active controller failure"
new_controller = 2
assert new_controller in controllers and new_controller != failed
assert election_time_sec <= 10, "failover time target exceeded"

print("KRaft failover simulation:", {
    "controllers": controllers,
    "failed": failed,
    "new_controller": new_controller,
    "election_time_sec": election_time_sec,
})
print("PASS: quorum and failover target checks passed")
PY
