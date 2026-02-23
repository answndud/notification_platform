# Chapter 17 - Replication/ISR/min.insync.replicas

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- Kafka 복제 구조와 ISR 의미를 설명할 수 있다.
- `min.insync.replicas`와 `acks` 관계를 이해할 수 있다.
- 내구성과 가용성 트레이드오프를 설계할 수 있다.

## 핵심 개념

Kafka는 파티션마다 리더/팔로워 복제를 사용합니다.
ISR은 리더와 충분히 동기화된 복제본 집합입니다.
`acks=all`과 `min.insync.replicas`는 데이터 안정성의 핵심 조합입니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic payment.events.v1
```

## 설계 포인트
- 중요 토픽은 replication factor와 min ISR을 함께 설계한다.
- 생산자 acks 설정과 토픽 정책을 일치시킨다.
- 장애 시 쓰기 실패 정책을 사용자 영향과 함께 정의한다.

## 자주 하는 실수
1. RF=1로 운영하다 장애 시 데이터 손실
2. acks=all인데 min ISR 설정 미흡
3. 내구성 설정 변경 후 부하 테스트 미수행

## 요약
- 복제/ISR은 Kafka 데이터 안정성의 중심이다.
- producer와 topic 설정을 함께 맞춰야 효과가 있다.

## 연습문제
### 기초
1. ISR이 무엇인지 설명해보세요.
2. acks와 min ISR 관계를 정리해보세요.

### 응용
1. 결제 토픽 내구성 설정안을 작성해보세요.
2. 쓰기 실패 시 사용자 응답 정책을 설계해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
