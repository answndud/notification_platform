# Chapter 25 - 보안: ACL/TLS/SASL

- 상태: 초안 완료
- 목표 분량: 9쪽

## 학습 목표
- Kafka 보안 기본 원칙을 설명할 수 있다.
- ACL 기반 권한 분리 전략을 수립할 수 있다.
- TLS/SASL 운영 체크리스트를 작성할 수 있다.

## 핵심 개념

Kafka 보안은 인증(SASL), 암호화(TLS), 인가(ACL)의 조합입니다.
하나라도 빠지면 공격면이 크게 늘어납니다.

## 최소 보안 기준표

| 환경 | TLS | SASL | ACL | 비밀 회전 |
|---|---|---|---|---|
| dev | 권장 | 권장 | 필수(서비스 계정 분리) | 분기 1회 |
| stg | 필수 | 필수 | 필수(토픽 단위 최소 권한) | 월 1회 |
| prod | 필수(mTLS 권장) | 필수 | 필수(읽기/쓰기/관리 분리) | 월 1회 + 즉시 폐기 절차 |

운영 기준:
- 내부망이라도 TLS/ACL을 생략하지 않습니다.
- 공용 계정 대신 서비스별 계정을 사용합니다.
- 계정 생성, 권한 부여, 회수 절차를 변경 이력으로 남깁니다.

## 실습 예제

실습 목적: `payment-service` 계정에 토픽 최소 권한을 부여하고 회수 절차를 익힙니다.

```bash
# 1) payment.events.v1 토픽 Read/Write 권한 부여
docker exec -it idea3-kafka kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:payment-service \
  --operation Read --operation Write --topic payment.events.v1

# 2) 현재 ACL 확인
docker exec -it idea3-kafka kafka-acls --bootstrap-server localhost:9092 \
  --list --topic payment.events.v1

# 3) 권한 회수(운영 종료/계정 폐기 시)
docker exec -it idea3-kafka kafka-acls --bootstrap-server localhost:9092 \
  --remove --allow-principal User:payment-service \
  --operation Read --operation Write --topic payment.events.v1
```

예상 결과/관찰 포인트:
- `--list`에서 `User:payment-service`의 토픽 권한이 보여야 합니다.
- 허용되지 않은 토픽 접근 시 권한 오류가 발생해야 정상입니다.
- 회수 후 재접근이 차단되면 권한 수명 관리가 정상 동작합니다.

## 실패 시 확인 항목

1. 인증 실패
- 증상: `SaslAuthenticationException`
- 확인: 계정/비밀번호, `sasl.mechanism`, JAAS 설정 일치 여부

2. 인가 실패
- 증상: `TopicAuthorizationException`, `GroupAuthorizationException`
- 확인: principal 이름, topic/group 패턴, allow/deny 중복 규칙

3. TLS 핸드셰이크 실패
- 증상: SSL handshake 관련 예외
- 확인: 인증서 만료일, SAN/CN, truststore/keystore 경로

## 설계 포인트
- 서비스별 계정을 분리하고 최소 권한을 적용한다.
- 운영자 권한은 별도 계정과 승인 절차로 통제한다.
- 인증 실패/권한 실패 로그를 경보와 연결한다.

권장 운영 체크리스트:
- 월 1회 계정/ACL 정기 검토(미사용 계정 회수 포함)
- 비밀 회전 후 연결 테스트 자동화
- 보안 이벤트(인증/인가 실패) 대시보드 분리

## 자주 하는 실수
1. 공용 계정 공유
2. 내부망이라는 이유로 TLS/ACL 생략
3. 키/인증정보 회전 정책 없음

## 요약
- Kafka 보안은 설정이 아니라 운영 체계다.
- 계정 분리와 감사 가능성이 핵심이다.

## 연습문제
### 기초
1. SASL/TLS/ACL 역할을 각각 설명해보세요.
2. 최소 권한 원칙을 Kafka에 적용해보세요.

### 응용
1. 서비스 계정 권한 표준안을 작성해보세요.
2. 월간 보안 점검 체크리스트를 설계해보세요.

## 예제 명령어 검증 시나리오(권장)

사전 조건:
- 테스트용 principal 2개(`payment-service`, `unauthorized-client`) 준비
- 검증 토픽 `payment.events.v1` 준비
- 브로커에 Authorizer 활성화(`authorizer.class.name`) 및 ACL 모드 적용
- 권장 실행: `docker compose -f docker-compose.idea3.yml -f docker-compose.kafka-sec.yml up -d zookeeper-sec kafka-sec`
- 빠른 검증: `./scripts/verify_kafka_acl.sh`

검증 절차:
1. `payment-service`에 `Read/Write` ACL 부여
2. `payment-service`로 produce/consume 성공 확인
3. `unauthorized-client`로 동일 토픽 접근 시도(실패 확인)
4. ACL 회수 후 `payment-service` 재접근 차단 확인

성공 기준:
- 허용 principal은 정상 동작, 비허용 principal은 권한 오류 발생
- ACL 회수 즉시 접근 차단 확인
- 인증/인가 실패 로그가 모니터링 경보로 연결됨

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
