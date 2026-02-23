# Chapter 14 - Pub/Sub와 Streams

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표

- Pub/Sub와 Streams의 전달 모델 차이를 설명할 수 있다.
- 실시간 알림과 이벤트 처리에 적절한 구조를 선택할 수 있다.
- 소비 실패/재처리 관점을 반영한 설계를 할 수 있다.

## 핵심 개념

### Pub/Sub

- 발행 시점에 구독 중인 클라이언트에게만 전달
- 저장되지 않음
- 매우 단순하고 빠름

Pub/Sub는 "현재 연결된 구독자" 중심 모델입니다.
구독자가 잠시 끊기면 그 사이 메시지는 복구되지 않습니다.

### Streams

- 메시지가 로그 형태로 저장
- 소비 그룹(consumer group) 지원
- 재처리/지연 소비 관리 가능

Streams는 "메시지 로그"를 보관하므로
소비자 재시작, 지연 처리, pending 추적이 가능합니다.
즉, Pub/Sub보다 운영 복원력이 높습니다.

### 선택 기준 요약

- 실시간 알림, 손실 허용 가능 -> Pub/Sub
- 처리 추적, 재시도, 소비자 그룹 필요 -> Streams
- 강한 내구성/생태계 요구 -> Kafka 같은 전용 브로커와 비교

## 실습 예제

Pub/Sub:

```bash
redis-cli -p 6380 SUBSCRIBE alarm:channel
redis-cli -p 6380 PUBLISH alarm:channel "hello"
```

Streams:

```bash
redis-cli -p 6380 XADD stream:order * orderId 1001 status created
redis-cli -p 6380 XRANGE stream:order - +
```

## 설계 포인트

- "놓치면 안 되는 이벤트"면 Pub/Sub 단독 사용을 피함
- Streams는 길이 관리와 소비자 장애 처리 정책이 필요
- 메시지 브로커와 비교해 보장 수준을 명확히 문서화

Streams 운영 핵심:

- 소비 그룹별 pending 모니터링
- 장기 미처리 메시지 claim 정책
- stream 길이 trim 정책

## 자주 하는 실수

1. Pub/Sub 메시지가 영구 저장된다고 오해
2. Streams pending 관리 없이 운영
3. 재처리 정책 없이 소비자만 늘림

## 요약

- Pub/Sub는 실시간, Streams는 로그형 소비에 강점이 있다.
- 기능 선택은 전달 보장 요구사항에서 시작해야 한다.

## 연습문제

### 기초

1. Pub/Sub로 알림 메시지 송수신을 실습해보세요.
2. Stream에 3개 이벤트를 쌓고 조회해보세요.

### 응용

1. 주문 이벤트에 Pub/Sub와 Streams 중 무엇이 적합한지 근거를 작성해보세요.
2. 소비자 장애 시 복구 절차를 설계해보세요.

## 챕터 체크리스트

- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
