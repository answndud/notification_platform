# Notification Platform Progress Log

## 2026-02-20

### Completed

1. Added recovery plan document: `PLAN.md`.
2. Added root entry guide: `README.md`.
3. Added executable evidence checker: `scripts/portfolio/verify_backend_evidence.sh`.
4. Ran quick verification:
   - Command: `bash scripts/portfolio/verify_backend_evidence.sh --quick`
   - Result: key Spring/Kafka code and Flyway files all detected.
5. Ran full verification:
   - Command: `bash scripts/portfolio/verify_backend_evidence.sh`
   - Result: `./gradlew :api:test :worker:test` succeeded.
6. Updated evidence map to include root-level implementation visibility and latest migrations (V5/V6):
   - File: `notification-platform-portfolio/22-code-test-evidence-map.md`
   - Added reproducible script-based verification commands.
7. Updated interview one-page sheet with implementation-proof commands:
   - File: `notification-platform-portfolio/23-one-page-interview-cheatsheet.md`
   - Added "3-minute demo commands" section for quick validation in interviews.
8. Added one-click API surface regression test script:
   - File: `scripts/portfolio/run_api_surface_tests.sh`
   - Command: `bash scripts/portfolio/run_api_surface_tests.sh`
   - Result: `BUILD SUCCESSFUL` (`:api:test` subset)
9. Added API surface verification report:
   - File: `API_SURFACE_VERIFICATION.md`
   - Scope: task/dlq/log endpoints + reproducible command.
10. Updated root README with FAQ for "설계는 좋은데 구현은?":
    - File: `README.md`
    - Included direct proof path via verification scripts.
11. Added end-to-end smoke automation script:
    - File: `scripts/portfolio/run_e2e_smoke.sh`
    - Covers request create -> task list -> task logs lookup.
12. Added E2E smoke runbook with latest execution status:
    - File: `E2E_SMOKE_RUNBOOK.md`
    - Current status: blocked only by missing local API/Worker runtime (`/actuator/health -> 000`).
13. Attempted to start local infra for full E2E evidence:
    - Command: `docker compose -f notification_platform/docker-compose.idea3.yml up -d`
    - Result: failed (`Cannot connect to the Docker daemon`).
    - Impact: full live E2E run is blocked by local Docker runtime availability.
14. Re-validated root proof scripts after updates:
    - Commands:
      - `bash scripts/portfolio/verify_backend_evidence.sh --quick`
      - `bash scripts/portfolio/run_api_surface_tests.sh`
    - Result: both succeeded (`[OK]` file checks + `BUILD SUCCESSFUL`).
15. Started Docker daemon and local infra, then launched API/Worker for live evidence run:
    - Commands:
      - `open -a Docker`
      - `docker compose -f notification_platform/docker-compose.idea3.yml up -d`
      - `nohup ./gradlew :api:bootRun ...`
      - `nohup ./gradlew :worker:bootRun ...`
16. Completed live E2E smoke successfully:
    - Command: `bash scripts/portfolio/run_e2e_smoke.sh`
    - Result:
      - `requestId=10`
      - `taskId=11`
      - task status `SENT`
      - task log resultCode `SENT_OK`
17. Per request, completed runtime shutdown/cleanup:
    - Commands:
      - `pkill -f "com.example.notification.ApiApplication"`
      - `pkill -f "com.example.notification.worker.WorkerApplication"`
      - `docker compose -f notification_platform/docker-compose.idea3.yml down`
      - `osascript -e 'quit app "Docker"'`
    - Final check: `docker info` exit code `1` (daemon stopped)
18. Added implementation inventory script to counter false "scripts-only" claims:
    - File: `scripts/portfolio/show_implementation_inventory.py`
    - Latest result:
      - `api_main_java_files=49`
      - `api_test_java_files=14`
      - `worker_main_java_files=15`
      - `migration_sql_files=6`
19. Added explicit implementation evidence index:
    - File: `IMPLEMENTATION_EVIDENCE.md`
    - Includes reproducible proof commands and key code anchors.
20. Updated root guide to include implementation evidence entrypoint:
    - File: `README.md`
    - Added inventory command and evidence index link.

### Why this matters

- The most critical interview risk was "implementation visibility".
- Root-level navigation now exposes where real API/Worker code exists.
- A single command now proves both code presence and test health.
- Endpoint-level regression proof is now also runnable via one script.

### In Progress

1. Continue feature work in nested service project with evidence-first updates.

### Next

1. Continue feature work in nested service project with test-backed increments.
2. Add measured latency delta evidence for interview numeric proof.
3. Add one forced-fail E2E variant to capture retry/DLQ proof automatically.
