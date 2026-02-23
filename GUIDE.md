# Notification Platform GUIDE

이 파일은 루트 문서를 정리한 **단일 진입 가이드**입니다.
실행 방법, 대시보드 테스트, 문제 해결, 문서 역할 요약을 한 번에 제공합니다.

## 1) 문서 구조 총정리

### 현재 루트 문서(유지)

| 파일 | 용도 | 언제 읽으면 좋은가 |
|---|---|---|
| `README.md` | 프로젝트 요약 + 빠른 시작 | 처음 저장소를 열었을 때 |
| `GUIDE.md` | 실행/검증/문서 인덱스 종합 가이드 | 실제 실행/테스트할 때 |
| `PLAN.md` | 작업 방향/우선순위/완료 기준 | 큰 작업 시작 전 |
| `PROGRESS.md` | 작업 이력/검증 기록 로그 | 최근 변경 추적 시 |
| `AGENT.md` | 구현 원칙, 아키텍처 규칙, Phase 기준 | 기능 구현/리팩토링 전에 |
| `OPENCODE_SESSION_INCIDENT_GUARD.md` | OpenCode 세션 분열 방지 운영 가이드 | OpenCode 세션 이슈 대응 시 |
| `PERFORMANCE_IMPROVEMENT_JOURNAL.md` | 성능 개선 스토리형 기록(레거시→Redis→Kafka) | 포트폴리오/면접 준비 시 |
| `BLOG_POST_FINAL.md` | 블로그 게시용 완성 원고 | 외부 게시/공유 시 |

### 이번 정리에서 루트에서 제거한 문서

아래 문서는 루트에서는 제거했고, 필요한 이력은 `docs/archive/`로 이동 보관했습니다.

- `IMPLEMENTATION_EVIDENCE.md` -> `docs/archive/IMPLEMENTATION_EVIDENCE.md`
- `API_SURFACE_VERIFICATION.md` -> `docs/archive/API_SURFACE_VERIFICATION.md`
- `E2E_SMOKE_RUNBOOK.md` -> `docs/archive/E2E_SMOKE_RUNBOOK.md`
- `notification_platform.md`
- `BLOG_POST_PACKAGE.md`

정리 원칙:
- 실행/검증 정보는 `GUIDE.md`로 통합
- 프로젝트 소개는 `README.md`로 축약
- 스토리 문서는 `PERFORMANCE_IMPROVEMENT_JOURNAL.md`, `BLOG_POST_FINAL.md`로 단일화

## 2) 실행 전 준비

- 프로젝트 루트: `notification_platform`
- 필수 도구: Java 21, Docker Desktop
- 기본 포트
  - API: `8080`
  - PostgreSQL: `5433`
  - Kafka: `9092`
  - Redis: `6380`

## 3) 로컬 실행

### IntelliJ 실행(권장)

1. IntelliJ에서 루트 폴더 `notification_platform` Open
2. Project SDK를 Java 21로 설정
3. IntelliJ 터미널에서 인프라 실행

```bash
docker compose -f docker-compose.idea3.yml up -d
```

4. `ApiApplication` 실행 (`api` 모듈)
5. `WorkerApplication` 실행 (`worker` 모듈)

### CLI 실행

```bash
./gradlew :api:bootRun
./gradlew :worker:bootRun
```

## 4) 대시보드 확인 및 기능 테스트

### 기본 접속

- URL: `http://localhost:8080/`
- 기대: `Operations Dashboard` 화면 표시

### 추천 테스트 순서

1. **Metrics 확인**
   - 우상단 `전체 새로고침`
   - `Pending/Sending/Sent/Failed/DLQ` 카드 값 확인
2. **요청 생성**
   - Request Key: `demo-dashboard-001` (매번 새 값 권장)
   - Template Code: `ORDER_PAID`
   - Receiver IDs: `1001`
   - Priority: `HIGH`
   - Variables: `{"orderNo":"A-100","amount":19900}`
3. **요청 조회**
   - `Key 조회` 또는 `최근 요청`
4. **Task 조회**
   - 생성된 requestId로 Task 조회
   - 상태가 `SENT`로 바뀌는지 확인
5. **DLQ 조회/Replay**
   - DLQ 목록 조회
   - 항목이 있으면 `Replay` 테스트

## 5) 빠른 검증 명령 모음

```bash
# 인프라 상태
docker compose -f docker-compose.idea3.yml ps

# API 기동 체크 (N404면 정상 기동 + 미존재 키)
curl "http://localhost:8080/api/v1/notifications/requests/by-key?requestKey=test"

# API/Worker 테스트
./gradlew :api:test :worker:test

# API 표면 회귀 테스트
bash scripts/portfolio/run_api_surface_tests.sh

# E2E 스모크
bash scripts/portfolio/run_e2e_smoke.sh
```

## 6) 문제 해결

### 요청 생성 시 `N503_MQ` 또는 내부 오류

- Kafka 연결 문제일 가능성이 큼
- 확인:

```bash
docker compose -f docker-compose.idea3.yml ps
```

- `kafka`, `zookeeper`, `postgres`가 Up이 아니면:

```bash
docker compose -f docker-compose.idea3.yml up -d
```

### 대시보드는 뜨는데 데이터가 비어 있음

- Worker 미기동 여부 확인
- API/Worker 로그 확인
- 브라우저 Network 탭에서 실패 API 확인

## 7) 종료

```bash
docker compose -f docker-compose.idea3.yml down
```

앱은 IntelliJ Stop 또는 터미널 `Ctrl + C`로 종료합니다.
