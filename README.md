# Notification Platform (Portfolio + Service Code)

This repository contains portfolio documentation at root and runnable service code in a nested Gradle project.

## Where the real implementation lives

- API service: `notification_platform/api`
- Worker service: `notification_platform/worker`
- Gradle root: `notification_platform/build.gradle`
- Infra compose: `notification_platform/docker-compose.idea3.yml`
- Evidence index: `IMPLEMENTATION_EVIDENCE.md`

## Implementation inventory (file counts)

```bash
python3 scripts/portfolio/show_implementation_inventory.py
```

## Quick verification from repo root

```bash
bash scripts/portfolio/verify_backend_evidence.sh --quick
```

## Full verification (includes tests)

```bash
bash scripts/portfolio/verify_backend_evidence.sh
```

## API surface regression (task/dlq/log)

```bash
bash scripts/portfolio/run_api_surface_tests.sh
```

## End-to-end smoke (request -> task -> task logs)

```bash
bash scripts/portfolio/run_e2e_smoke.sh
```

Prerequisite: local API/Worker must be running.

## Interview demo minimum path

1. Start infra: `docker compose -f notification_platform/docker-compose.idea3.yml up -d`
2. Run tests: `./gradlew :api:test :worker:test` (inside `notification_platform`)
3. Execute request->consume->db verification scenario from portfolio docs

## Interview FAQ: "설계는 좋은데 구현은?"

- 구현은 `notification_platform/api`, `notification_platform/worker`에 존재합니다.
- 루트에서 `bash scripts/portfolio/verify_backend_evidence.sh`를 실행하면 코드 존재 + 테스트 통과를 함께 증명합니다.
- 추가로 `bash scripts/portfolio/run_api_surface_tests.sh`로 task/dlq/log API 회귀 테스트를 재현할 수 있습니다.
- 실행 환경이 준비되면 `bash scripts/portfolio/run_e2e_smoke.sh`로 요청부터 task/log 조회까지 E2E 스모크를 재현할 수 있습니다.
