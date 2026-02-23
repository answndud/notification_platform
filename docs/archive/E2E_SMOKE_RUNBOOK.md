# E2E Smoke Runbook (Archived)

이 문서는 루트 정리 과정에서 `docs/archive/`로 이동한 보관본입니다.

## Goal

Verify minimal end-to-end flow:

1. Create notification request
2. Fetch delivery tasks by request
3. Fetch task logs

## Command

```bash
bash scripts/portfolio/run_e2e_smoke.sh
```

## Prerequisites

1. `docker compose -f notification_platform/docker-compose.idea3.yml up -d`
2. Start API app (`notification_platform/api`)
3. Start Worker app (`notification_platform/worker`)

## Latest Execution (2026-02-20)

- Command executed from repo root: `bash scripts/portfolio/run_e2e_smoke.sh`
- Result: success
  - `requestId=10`
  - `taskId=11`
  - `tasks.status=SENT`
  - `task_logs.resultCode=SENT_OK`
- Evidence source: script output in CLI session (request -> task -> logs)

## Infra Start Attempt (2026-02-20)

- Command: `docker compose -f notification_platform/docker-compose.idea3.yml up -d`
- Result: failed (`Cannot connect to the Docker daemon`)
- Recovery: start Docker daemon, then rerun infra command and E2E smoke script

## Session Cleanup (2026-02-20)

- Stopped service processes:
  - `pkill -f "com.example.notification.ApiApplication"`
  - `pkill -f "com.example.notification.worker.WorkerApplication"`
- Stopped infra:
  - `docker compose -f notification_platform/docker-compose.idea3.yml down`
- Quit Docker Desktop:
  - `osascript -e 'quit app "Docker"'`
- Final check:
  - `docker info` exit code `1` (daemon stopped)
