# Kafka Guide 검증 로그

## 목적

- 가이드북 챕터 예제 명령(`kafka-topics`, `kafka-console-producer`, `kafka-console-consumer`, `kcat`) 실실행 검증

## 현재 상태

- [x] 로컬 Kafka 컨테이너 기동 확인
- [x] Part 01~03 명령 예제 검증(일부)
- [x] Part 04 운영 점검 명령 검증(일부)
- [ ] Part 05~06 통합 시나리오 검증

## 2026-02-23 1차 검증 결과

실행 환경:

- `docker compose -f notification_platform/docker-compose.idea3.yml up -d` 성공
- 검증 대상 컨테이너: `idea3-kafka`

검증 완료 명령:

1. Topic 생성/조회
- `kafka-topics --create --if-not-exists`
- `kafka-topics --describe`
- `kafka-topics --list`

2. Producer/Consumer 콘솔
- `kafka-console-producer`로 `quickstart`, `order.events`, `payment.events.v1` 메시지 전송
- `kafka-console-consumer --from-beginning --max-messages`로 수신 확인

3. Group/Offset 점검
- `kafka-consumer-groups --describe --group payment-workers`
- `kafka-consumer-groups --list`

4. Topic 정책 변경
- `kafka-configs --alter --add-config retention.ms=86400000`
- `kafka-configs --describe`

검증 중 관찰 사항:

- `payment-workers` 그룹 소비 검증에서 `TimeoutException` 로그가 1회 발생했지만,
  group describe 결과 `CURRENT-OFFSET` 증가로 메시지 처리 자체는 확인됨
- 토픽 이름에 `.` 포함 시 metric naming 경고가 출력됨(동작에는 영향 없음)

체크 반영:

- 예제 명령 검증 완료(`[x]`) 반영 챕터
  - `ch01`~`ch10`
  - `ch16`
  - `ch17`
  - `ch20`

보류 챕터:

- `ch11` 스키마 레지스트리: 별도 SR 환경 필요
- `ch12`~`ch15`: 애플리케이션/심화 시나리오 검증 필요
- `ch18`, `ch19`, `ch21`: 멀티 브로커/KRaft/재배치 실험 토폴로지 필요
- `ch22`~`ch30`: 성능/보안/Spring 통합/사례형 검증 시나리오 필요

## 2026-02-23 2차 검증 결과

추가 검증 완료 명령:

1. Producer 고급 속성 전달
- `kafka-console-producer --producer-property acks=all --producer-property retries=3 --producer-property linger.ms=5 --producer-property batch.size=32768 --producer-property compression.type=lz4`

2. DLQ 흐름 검증
- `payment.events.dlq` 토픽 생성
- 실패 이벤트 전송/소비 확인

3. Lag 관측 및 감소 시나리오
- `payment.events.v1`에 20건 추가 전송
- `kafka-consumer-groups --describe`로 lag 증가 확인
- 그룹 소비 후 lag 감소 확인(20 -> 12)

4. Hot partition/skew 관측
- key 기반 편중 메시지(`userA` 다건, `userB` 소수) 전송
- `kafka-console-consumer --property print.partition=true --property print.key=true`로 파티션 편중 확인

체크 반영:

- 추가 `[x]` 반영 챕터
  - `ch12`, `ch14`, `ch23`, `ch24`

## 메모

- 멀티 브로커/KRaft 실습은 별도 토폴로지 필요
