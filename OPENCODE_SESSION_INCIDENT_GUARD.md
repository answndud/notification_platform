# OpenCode Session Incident Guard

OpenCode에서 `project_id` 분열로 기존 세션이 숨겨지는 문제를 재발 방지하기 위한 운영 가이드입니다.

## 왜 이 문서/스크립트를 추가했나

- 세션 삭제가 아니라 `project.worktree` 중복 생성으로 `session.project_id`가 분열되면 UI 프로젝트 필터에서 세션이 사라진 것처럼 보일 수 있습니다.
- 즉시 탐지, 사전 예방, 안전 복구를 표준화하면 동일 장애를 반복하지 않을 수 있습니다.

## 1) OpenCode 시작 전 고정 체크

아래 3가지를 먼저 맞춘 뒤 세션을 시작합니다.

1. 저장소 정체성 고정
   - `git init` -> `git remote add origin <url>` -> `git push -u origin <branch>` 완료
2. 경로 정규화
   - 항상 동일한 `realpath` 경로로만 진입 (symlink/별칭 경로 금지)
3. 필터 안전 운용
   - 가능하면 OpenCode에서 Global/All 세션 기반으로 확인하고, 단일 프로젝트 필터 의존을 낮춤

## 2) 정기 점검 (중복 감시)

루트에서 실행:

```bash
./scripts/opencode/opencode_preflight.sh
```

의미:
- `git_identity: OK` 여야 함 (remote/upstream 확인)
- `worktree_duplicates: OK` 여야 함
- `session_distribution_for_directory_prefix`는 보통 1개 `project_id`로 모여야 함

직접 감사만 하고 싶으면:

```bash
python3 scripts/opencode/opencode_project_guard.py --worktree "$(pwd)"
```

권장 주기:
- OpenCode로 본격 작업 시작 전 1회
- 큰 Git 메타 변경(브랜치 재설정, remote 변경, upstream 재연결) 직후 1회

## 3) 이상 감지 시 즉시 복구

1) 우선 OpenCode 프로세스를 완전히 종료

2) 대상 worktree 확인 후 감사 실행

```bash
python3 scripts/opencode/opencode_project_guard.py --worktree "$(pwd)"
```

3) 복구 실행 (자동 백업 생성)

```bash
python3 scripts/opencode/opencode_project_guard.py \
  --worktree "$(pwd)" \
  --repair \
  --target-project-id "<정상_project_id>"
```

주의:
- `--repair`는 `session.project_id`를 update 하므로 반드시 OpenCode 종료 후 실행
- 복구 전 DB 백업(`*.bak`)을 자동 생성

## 4) 운영 원칙

- Git commit/push 자체가 직접 원인은 아님
- 다만 upstream/remote 메타가 변하는 순간 프로젝트 식별이 흔들릴 수 있으므로, 위 정체성 고정 절차를 루틴화
- 동일 장애 재발 시 수동 SQL보다 본 스크립트를 우선 사용해 표준 절차로 처리
