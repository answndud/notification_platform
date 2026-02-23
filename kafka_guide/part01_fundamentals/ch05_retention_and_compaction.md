# Chapter 05 - Retention과 Compaction

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표
- retention과 compaction 차이를 설명할 수 있다.
- 토픽별 보관 정책을 설계할 수 있다.
- 데이터 수명과 비용을 함께 고려할 수 있다.

## 핵심 개념

- Retention: 시간/크기 기준으로 로그 삭제
- Compaction: 키 기준 최신 값 중심 정리

Retention은 보관 기간,
Compaction은 상태 최신화 전략에 가깝습니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name order.events --alter --add-config retention.ms=86400000
```

## 설계 포인트
- 감사/재처리 요구가 크면 retention을 길게 잡는다.
- 상태 동기화 토픽은 compaction을 검토한다.
- 정책 변경은 비용/복구 영향과 함께 판단한다.

## 자주 하는 실수
1. 기본 retention에만 의존
2. compaction과 삭제 정책 혼동
3. 토픽 목적과 정책 불일치

## 요약
- 보관 정책은 운영 비용과 복구 전략의 중심이다.
- 토픽 목적에 맞는 정책 선택이 필요하다.

## 연습문제
### 기초
1. retention과 compaction 차이를 설명해보세요.
2. 로그성 토픽 retention 예시를 작성해보세요.

### 응용
1. 사용자 상태 이벤트 토픽 정책을 설계해보세요.
2. 정책 변경 영향 분석 항목을 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
