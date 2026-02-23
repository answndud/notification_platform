# Chapter 08 - Consumer/Group 기초

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- Consumer Group 동작 원리를 설명할 수 있다.
- 파티션 할당과 병렬 처리 관계를 이해할 수 있다.
- 소비자 확장 전략을 수립할 수 있다.

## 핵심 개념

같은 그룹의 컨슈머는 파티션을 분담해 읽습니다.
파티션 수가 컨슈머 수보다 적으면 일부 컨슈머는 유휴 상태가 됩니다.

## 직관 그림

```text
Topic payment.events.v1 (Partitions: 0,1,2)

Group: payment-workers
  Consumer-1 -> P0
  Consumer-2 -> P1
  Consumer-3 -> P2

Consumer-4 추가 시:
  Consumer-4 -> (할당 없음, idle)
```

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.events.v1 --group payment-workers

docker exec -it idea3-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group payment-workers
```

## 설계 포인트
- 그룹 단위 책임 경계를 명확히 정의한다.
- 소비 처리시간 편차가 크면 파티션 재분배 비용을 고려한다.
- 장애 복구 시 offset/재처리 정책을 런북과 연결한다.

## 자주 하는 실수
1. 그룹 개념 없이 컨슈머를 무작정 늘림
2. 파티션 수보다 많은 인스턴스를 띄움
3. lag 지표 없이 운영

## 요약
- Consumer Group은 Kafka 확장성과 복원성의 중심이다.
- 파티션 수와 소비자 수의 관계를 항상 함께 봐야 한다.

## 초보자 체크
- "같은 그룹"과 "다른 그룹" 소비 차이를 설명할 수 있는가?
- idle consumer가 생기는 조건을 설명할 수 있는가?

## 연습문제
### 기초
1. 그룹과 파티션 할당 관계를 설명해보세요.
2. lag가 의미하는 운영 신호를 적어보세요.

### 응용
1. 주문 처리 소비자 확장 계획을 작성해보세요.
2. 소비자 장애 시 복구 절차를 정리해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
