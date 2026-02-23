# Chapter 01 - Kafka란 무엇이고, 왜 필요한가

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표
- Kafka를 한 문장으로 설명할 수 있다.
- Kafka와 DB/메시지 큐의 역할 차이를 구분할 수 있다.
- Kafka를 써야 하는 경우와 피해야 하는 경우를 판단할 수 있다.

## 핵심 개념

Kafka는 분산 로그 기반 이벤트 스트리밍 플랫폼입니다.
핵심은 이벤트를 순서 있는 로그로 저장하고,
여러 소비자가 각자 오프셋으로 독립적으로 읽을 수 있다는 점입니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic demo.events --partitions 3 --replication-factor 1
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic demo.events
```

관찰 포인트:
- Topic/Partition이 저장/확장 단위라는 점
- Consumer는 오프셋으로 소비 위치를 관리한다는 점

## 설계 포인트
- 이벤트 재처리 요구가 크면 Kafka 적합성이 높다.
- 동기 트랜잭션 정합성이 핵심이면 DB/동기 API를 우선 고려한다.
- 도입 전 이벤트 계약(스키마/버전/보장수준)을 먼저 정의한다.

## 자주 하는 실수
1. Kafka를 단순 큐로만 이해
2. 계약 없이 토픽부터 생성
3. 실패/재처리 정책 없이 운영 진입

## 요약
- Kafka는 이벤트 저장/전달을 분리하는 플랫폼이다.
- 설계의 핵심은 토픽 구조, 소비 전략, 운영 정책이다.

## 연습문제
### 기초
1. Kafka를 2문장으로 설명해보세요.
2. Kafka와 RDBMS 차이를 3가지 적어보세요.

### 응용
1. 본인 시스템의 Kafka 도입 후보 이벤트 3개를 선정해보세요.
2. Kafka 비적합 영역을 1개 선정해 근거를 적어보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
