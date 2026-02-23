#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path


def count_files(root: Path, suffix: str) -> int:
    return sum(1 for p in root.rglob(f"*{suffix}") if p.is_file())


def main() -> int:
    repo_root = Path(__file__).resolve().parents[2]
    service_root = repo_root / "notification_platform"
    api_main = service_root / "api" / "src" / "main" / "java"
    api_test = service_root / "api" / "src" / "test" / "java"
    worker_main = service_root / "worker" / "src" / "main" / "java"
    migration = service_root / "api" / "src" / "main" / "resources" / "db" / "migration"

    key_paths = [
        service_root / "build.gradle",
        service_root / "settings.gradle",
        service_root / "api" / "src" / "main" / "java" / "com" / "example" / "notification" / "ApiApplication.java",
        service_root / "worker" / "src" / "main" / "java" / "com" / "example" / "notification" / "worker" / "WorkerApplication.java",
        service_root / "api" / "src" / "main" / "java" / "com" / "example" / "notification" / "domain" / "request" / "controller" / "NotificationRequestController.java",
        service_root / "worker" / "src" / "main" / "java" / "com" / "example" / "notification" / "worker" / "consumer" / "NotificationRequestQueuedConsumer.java",
        service_root / "api" / "src" / "main" / "resources" / "db" / "migration" / "V1__init_notification_request.sql",
        service_root / "api" / "src" / "main" / "resources" / "db" / "migration" / "V6__add_delivery_task_idempotency_constraint.sql",
    ]

    print("== Implementation Inventory ==")
    print(f"repo_root={repo_root}")
    print(f"service_root={service_root}")
    print(f"api_main_java_files={count_files(api_main, '.java')}")
    print(f"api_test_java_files={count_files(api_test, '.java')}")
    print(f"worker_main_java_files={count_files(worker_main, '.java')}")
    print(f"migration_sql_files={count_files(migration, '.sql')}")

    print("key_files:")
    for path in key_paths:
        print(f"- {'OK' if path.exists() else 'MISSING'} {path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
