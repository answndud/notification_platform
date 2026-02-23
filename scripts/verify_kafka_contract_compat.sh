#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
import json

v1 = {
    "eventType": "PAYMENT_CREATED",
    "paymentId": "p-1001",
    "amount": 12000,
}

v2 = {
    "eventType": "PAYMENT_CREATED",
    "paymentId": "p-1001",
    "amount": 12000,
    "currency": "KRW",  # optional field added
}

required_v1 = {"eventType", "paymentId", "amount"}

def validate_required(payload, required):
    missing = required - set(payload.keys())
    if missing:
        raise ValueError(f"missing fields: {sorted(missing)}")

validate_required(v1, required_v1)
validate_required(v2, required_v1)

print("v1 payload:", json.dumps(v1, ensure_ascii=False))
print("v2 payload:", json.dumps(v2, ensure_ascii=False))
print("PASS: backward compatible field set validated")
PY
