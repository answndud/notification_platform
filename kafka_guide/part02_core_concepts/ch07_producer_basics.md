# Chapter 07 - Producer 기초

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- Producer 전송 흐름을 설명할 수 있다.
- acks/retries/batch/compression 기본값을 이해할 수 있다.
- 전송 실패 시 처리 전략을 설계할 수 있다.

## 핵심 개념

Producer는 레코드를 생성해 파티션으로 전송합니다.
핵심 설정은 안정성(acks/retries)과 성능(batch/linger/compression) 균형입니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic payment.events.v1
```

입력 예시:
```text
{"eventId":"e1","orderId":1001,"amount":12000}
```

## 설계 포인트
- 운영 기본은 `acks=all` + 적절한 retries를 권장한다.
- 고트래픽 토픽은 compression을 기본 검토한다.
- 키 전략을 명확히 정해 순서 요구를 만족한다.

## 자주 하는 실수
1. 기본 설정으로만 운영하고 장애 시 재전송 정책 없음
2. 메시지 키 없이 전송해 순서 요구 불만족
3. 오류 콜백/메트릭 수집 미구현

## 요약
- Producer 설계는 안정성/지연/비용의 균형 문제다.
- 실패 처리와 키 전략을 함께 설계해야 한다.

## 연습문제
### 기초
1. acks 설정 3가지 차이를 비교해보세요.
2. 메시지 키를 넣었을 때의 효과를 설명해보세요.

### 응용
1. 결제 이벤트 프로듀서 기본 설정을 제안해보세요.
2. 전송 실패 대응 절차를 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
