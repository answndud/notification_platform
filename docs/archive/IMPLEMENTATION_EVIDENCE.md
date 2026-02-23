# Implementation Evidence (Archived)

이 문서는 루트 정리 과정에서 `docs/archive/`로 이동한 보관본입니다.

## Where implementation code is

- Service root: `notification_platform/`
- API code: `notification_platform/api/src/main/java`
- Worker code: `notification_platform/worker/src/main/java`
- DB migrations: `notification_platform/api/src/main/resources/db/migration`

## Inventory command

```bash
python3 scripts/portfolio/show_implementation_inventory.py
```

## Latest inventory result (2026-02-20)

- `api_main_java_files=49`
- `api_test_java_files=14`
- `worker_main_java_files=15`
- `migration_sql_files=6`

Key files confirmed:

- `notification_platform/build.gradle`
- `notification_platform/settings.gradle`
- `notification_platform/api/src/main/java/com/example/notification/ApiApplication.java`
- `notification_platform/worker/src/main/java/com/example/notification/worker/WorkerApplication.java`
- `notification_platform/api/src/main/java/com/example/notification/domain/request/controller/NotificationRequestController.java`
- `notification_platform/worker/src/main/java/com/example/notification/worker/consumer/NotificationRequestQueuedConsumer.java`

## Reproducible proof commands

- Existence + module tests:
  - `bash scripts/portfolio/verify_backend_evidence.sh`
- API surface regression:
  - `bash scripts/portfolio/run_api_surface_tests.sh`
- End-to-end smoke:
  - `bash scripts/portfolio/run_e2e_smoke.sh`
