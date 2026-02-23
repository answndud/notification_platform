# Chapter 03 - Broker/Topic/Partition 개요

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표
- Broker/Topic/Partition 관계를 설명할 수 있다.
- 파티션이 처리량/순서에 미치는 영향을 이해할 수 있다.
- 이벤트 흐름을 단계별로 설명할 수 있다.

## 핵심 개념

- Broker: Kafka 서버 노드
- Topic: 이벤트 논리 그룹
- Partition: Topic 물리 분할 단위

파티션 확장은 처리량을 높이지만,
순서는 파티션 단위로만 보장됩니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic order.events --partitions 3 --replication-factor 1
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic order.events
```

## 설계 포인트
- 파티션 수는 피크 처리량과 소비 병렬성 기준으로 결정한다.
- 순서 중요 이벤트는 키 전략과 함께 설계한다.
- 파티션 증설 후 운영 비용(리밸런스/재배치)을 고려한다.

## 자주 하는 실수
1. 파티션 수 과소 설계
2. 순서 요구가 있는데 키 전략 없음
3. 토픽 목적/정책 문서화 누락

## 요약
- 파티션은 Kafka 확장성의 핵심이다.
- 처리량과 순서 요구를 동시에 고려해야 한다.

## 연습문제
### 기초
1. Topic/Partition/Broker 관계를 그림으로 설명해보세요.
2. 파티션 1개와 3개의 차이를 정리해보세요.

### 응용
1. 주문 이벤트 토픽 파티션 수를 제안해보세요.
2. 증설 시 점검 체크리스트를 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
