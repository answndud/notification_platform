# Chapter 09 - Offset 커밋과 Rebalance

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- auto/manual commit 차이를 이해할 수 있다.
- 중복/유실 리스크를 커밋 시점과 연결해 설명할 수 있다.
- rebalance가 서비스에 주는 영향을 관리할 수 있다.

## 핵심 개념

Offset은 "어디까지 처리했는가"의 기준입니다.
커밋 타이밍이 너무 빠르면 유실 위험,
너무 늦으면 중복 처리 비용이 증가합니다.

Rebalance는 컨슈머 그룹 구성 변경 시 파티션 재할당 과정입니다.
재할당 중 처리 지연/중단 구간이 발생할 수 있습니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group payment-workers
```

## 설계 포인트
- 핵심 처리 완료 후 커밋 원칙을 명확히 한다.
- 처리 시간이 긴 작업은 idempotent 소비자로 설계한다.
- 배포 시 동시 재시작으로 rebalance 폭발이 나지 않게 롤링 전략 적용.

## 자주 하는 실수
1. auto-commit만 쓰고 처리 성공 조건을 통제하지 않음
2. rebalance 이벤트를 관측하지 않음
3. 중복 처리 허용 여부를 도메인별로 정의하지 않음

## 요약
- offset 정책은 데이터 정합성과 직결된다.
- rebalance는 정상 동작이지만, 통제하지 않으면 지연 원인이 된다.

## 연습문제
### 기초
1. auto commit과 manual commit을 비교해보세요.
2. rebalance가 발생하는 조건을 적어보세요.

### 응용
1. 수동 커밋 기준 절차를 작성해보세요.
2. 배포 중 rebalance 영향을 줄이는 계획을 설계해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
