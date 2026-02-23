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

## 2026-02-23 문서 보강 반영 후 상태 동기화

- 보강 챕터: `ch13`, `ch20`, `ch25`, `ch29`, `ch30`
- 검증 상태 반영:
  - `[x]` 유지: `ch20` (consumer group lag 점검 명령 검증 이력 존재)
  - `[ ]` 유지: `ch13`, `ch25`, `ch29`, `ch30` (통합/보안/사례형 검증 시나리오 필요)
- 확장 보강 챕터: `ch22`, `ch26`, `ch28` (예제/실패 점검 템플릿 추가, 실실행 검증은 보류)

## 2026-02-23 3차 검증 시도(보강 챕터 기준)

실행 환경:

- `docker compose -f docker-compose.idea3.yml up -d` 성공
- 컨테이너: `idea3-kafka`

검증 시도 결과:

1. `ch25` ACL 시나리오
- 실행: `kafka-acls --add/--list/--remove`
- 결과: `SecurityDisabledException: No Authorizer is configured`
- 판단: 현재 토폴로지는 Authorizer 비활성 상태로 ACL 실검증 불가

2. `ch13` EOS 시나리오
- 확인: 레포 내 트랜잭션 producer 샘플/테스트 경로 부재
- 판단: `transactional.id` 기반 전송 검증은 별도 샘플 앱 필요

3. `ch29`, `ch30` 런북/사례 시나리오
- 확인: 문서형 검증 절차는 추가 완료
- 판단: 온콜 드릴/전후 지표 수집을 위한 운영 리허설 슬롯 필요

체크 반영:

- `[x]` 신규 전환 없음
- `[ ]` 유지 챕터: `ch13`, `ch25`, `ch29`, `ch30`

## 2026-02-23 4차 검증 결과(보강 챕터 재검증)

실행 환경:

- 기본 브로커: `idea3-kafka` (`localhost:9092`)
- 보안 브로커(ACL 검증 전용): `docker-compose.kafka-sec.yml`의 `idea3-kafka-sec` (`localhost:19092`)

검증 완료 항목:

1. `ch13` EOS 시나리오(샘플 스크립트 기반)
- 실행: `./scripts/verify_kafka_eos.sh`
- 결과: `kafka-producer-perf-test` 트랜잭션 전송 성공, `read_uncommitted`/`read_committed` 샘플 소비 확인

2. `ch25` ACL 시나리오(Authorizer 활성화 브로커)
- 실행: `./scripts/verify_kafka_acl.sh`
- 결과: ACL add/list/remove 전 과정 성공 확인

체크 반영:

- `[x]` 신규 전환 챕터: `ch13`, `ch25`
- `[ ]` 유지 챕터: `ch29`, `ch30` (운영 드릴/전후 지표 측정 필요)
