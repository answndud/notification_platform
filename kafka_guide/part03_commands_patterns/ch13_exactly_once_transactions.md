# Chapter 13 - Exactly-Once와 트랜잭션

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- EOS(Exactly-Once Semantics) 개념을 설명할 수 있다.
- idempotent producer와 transaction의 관계를 이해한다.
- 적용 범위를 현실적으로 결정할 수 있다.

## 핵심 개념

Exactly-Once는 "중복 제거 + 원자적 커밋"의 조합입니다.
하지만 복잡도와 비용이 높아 모든 경로에 적용하는 전략은 비효율적일 수 있습니다.

## 직관 그림

```text
일반 at-least-once:
  전송 성공 후 재시도 가능 -> 중복 가능

EOS:
  idempotent producer + transaction 경계
  -> 중복/유실 제어 강화 (대신 운영 복잡도 증가)
```

핵심 판단 질문:
- 중복 1건이 치명적인가?
- 성능 저하/운영 복잡도를 감당할 가치가 있는가?

## 실습 예제

```properties
enable.idempotence=true
transactional.id=payment-tx-producer-1
acks=all
```

관찰 포인트:
- 트랜잭션 경계가 깨지면 처리 지연과 운영 복잡도가 증가함

## 설계 포인트
- 핵심 금전/정산 이벤트만 우선 적용한다.
- 일반 이벤트는 at-least-once + idempotent 소비로 충분한지 검토한다.
- 트랜잭션 장애 시 복구 절차를 문서화한다.

## 자주 하는 실수
1. EOS를 만능처럼 적용
2. 트랜잭션 타임아웃/에러 경로 미검증
3. 소비자 idempotency 없이 producer 설정만 강화

## 요약
- EOS는 강력하지만 비싸다.
- 도메인 위험도 기반으로 선택해야 한다.

## 초보자 체크
- EOS를 쓰지 않아도 안전하게 만들 방법(idempotent 소비)을 설명할 수 있는가?
- "어디에 EOS를 쓰고, 어디는 안 쓸지" 기준을 제시할 수 있는가?

## 연습문제
### 기초
1. idempotence와 transaction 차이를 설명해보세요.
2. EOS가 필요한 케이스를 1개 제시해보세요.

### 응용
1. 결제 이벤트에 EOS 적용 범위를 설계해보세요.
2. 비적용 경로의 보완책을 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [ ] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
