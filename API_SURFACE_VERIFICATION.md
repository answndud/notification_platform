# API Surface Verification Report (Task/DLQ/Logs)

## Date

2026-02-20

## Scope

- `GET /api/v1/notifications/tasks`
- `GET /api/v1/notifications/tasks/{id}`
- `POST /api/v1/notifications/tasks/{id}/retry`
- `GET /api/v1/notifications/tasks/{id}/logs`
- `GET /api/v1/notifications/dlq`
- `GET /api/v1/notifications/dlq/{id}`
- `POST /api/v1/notifications/dlq/{id}/replay`
- `GET /api/v1/notifications/dlq/{id}/logs`

## Code Anchors

- `notification_platform/api/src/main/java/com/example/notification/domain/task/controller/DeliveryTaskController.java`
- `notification_platform/api/src/main/java/com/example/notification/domain/dlq/controller/DlqController.java`
- `notification_platform/api/src/main/java/com/example/notification/domain/task/service/DeliveryTaskService.java`
- `notification_platform/api/src/main/java/com/example/notification/domain/dlq/service/DlqService.java`

## Verification Command

```bash
bash scripts/portfolio/run_api_surface_tests.sh
```

## Result

- Build result: `BUILD SUCCESSFUL`
- Test target: `DeliveryTaskControllerTest`, `DeliveryTaskServiceTest`, `DlqControllerTest`, `DlqServiceTest`
- Verification outcome: API contract + filter validation + error mapping + log pagination regression checks passed

## Notes

- This report is command-reproducible from repository root.
- For full backend existence + all module tests, run `bash scripts/portfolio/verify_backend_evidence.sh`.
