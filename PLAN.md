# Notification Platform Recovery Plan

## Objective

Turn this repository from a documentation-heavy portfolio into an interview-safe project where claims are backed by runnable Spring/Kafka code, automated tests, and reproducible evidence.

## Current Facts (2026-02-20)

- Main implementation exists in `notification_platform/` (nested project).
- Portfolio docs in the repo root do not clearly expose that implementation.
- Interview risk: reviewers may stop at root level and conclude there is no service code.

## Streams

### Stream A - Visibility and Structure

1. Add root-level entry documentation that points directly to service code.
2. Add a verification script that validates key files and can run tests.
3. Make evidence navigation obvious for interviewers in under 2 minutes.

Done when:
- A new reviewer can find API/Worker code paths and run verification commands from root.

### Stream B - Evidence Quality Upgrade

1. Refresh evidence mapping to include latest migrations and APIs.
2. Standardize each claim as: claim -> code path -> command -> expected result.
3. Keep a single source of truth for demo commands.

Done when:
- Every major claim in portfolio docs has at least one code path and one reproducible command.

### Stream C - Implementation Continuation

1. Continue feature development in `notification_platform/api` and `notification_platform/worker`.
2. Keep `:api:test` and `:worker:test` green on each increment.
3. Track feature deltas in `PROGRESS.md` with objective evidence.

Done when:
- New functionality lands with tests and appears in evidence docs on the same day.

## Delivery Checklist

### Phase 1 (today)

- [x] Create `PLAN.md`.
- [x] Create/update `PROGRESS.md` with live progress log.
- [x] Add root `README.md` with code and command entry points.
- [x] Add `scripts/portfolio/verify_backend_evidence.sh`.
- [x] Run verification script and record outputs.

### Phase 2 (next)

- [x] Update `notification-platform-portfolio/22-code-test-evidence-map.md` with latest paths and migrations.
- [x] Add one-click demo command set for interview flow.
- [x] Add concise FAQ section for "design vs implementation" challenge.

### Phase 3 (ongoing)

- [ ] Continue domain feature implementation.
- [ ] Add/maintain integration tests for enqueue->consume->task/log.
- [ ] Add measured performance deltas to evidence docs.

## Working Rules

- Do not claim completion without command output or test result.
- Keep root docs synchronized with nested project reality.
- Update `PROGRESS.md` immediately when a checklist item is completed.
