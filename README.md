# Notification Platform

알림 요청을 비동기 파이프라인(API + Worker + Kafka + PostgreSQL)으로 처리하고,
재시도/DLQ/운영 지표를 함께 검증할 수 있는 백엔드 프로젝트입니다.

## 빠른 시작

1. 인프라 실행

```bash
docker compose -f docker-compose.idea3.yml up -d
```

2. 애플리케이션 실행

```bash
./gradlew :api:bootRun
./gradlew :worker:bootRun
```

3. 대시보드 접속

- `http://localhost:8080/`

## 문서 안내

- 실행/테스트/문서 인덱스: `GUIDE.md`
- 개발/구현 원칙: `AGENT.md`
- 성능 개선 서사 기록: `PERFORMANCE_IMPROVEMENT_JOURNAL.md`
