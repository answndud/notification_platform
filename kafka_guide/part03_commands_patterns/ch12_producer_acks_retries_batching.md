# Chapter 12 - Producer acks/retries/batching

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- acks/retries/batch/linger 설정의 의미를 이해한다.
- 지연과 안정성 간 트레이드오프를 설명할 수 있다.
- 기본 튜닝 템플릿을 만들 수 있다.

## 핵심 개념

- `acks`: 리더/복제 확인 수준
- `retries`: 일시 오류 재전송 횟수
- `batch.size`, `linger.ms`: 처리량과 지연의 균형

## 직관 그림

```text
지연 최소화 우선:
  linger.ms 낮음, batch 작음 -> 빠른 전송, 처리량 낮을 수 있음

처리량 우선:
  linger.ms 높음, batch 큼 -> 묶음 전송, 지연 증가 가능
```

초보자 실무 팁:
- 먼저 안정성(acks/retries)을 맞추고,
- 그 다음 처리량(batch/linger/compression)을 조정합니다.

## 실습 예제

```bash
# 애플리케이션 producer 설정 예시
acks=all
retries=10
linger.ms=5
batch.size=65536
compression.type=lz4
```

## 설계 포인트
- 금융/결제 경로는 안정성 우선 설정을 사용한다.
- 로그/분석 경로는 처리량 중심 설정을 검토한다.
- 토픽별 설정 프로파일을 분리해 운영한다.

## 자주 하는 실수
1. 모든 토픽에 동일 설정 사용
2. 튜닝 전 baseline 미측정
3. 전송 실패 메트릭 미수집

## 요약
- Producer 튜닝은 도메인별로 달라야 한다.
- 안정성/지연/비용 균형을 명시적으로 선택해야 한다.

## 초보자 체크
- acks, retries, linger.ms를 각각 왜 조정하는지 말할 수 있는가?
- 튜닝 전 baseline 지표 3개를 정할 수 있는가?

## 연습문제
### 기초
1. acks=1과 acks=all 차이를 설명해보세요.
2. linger.ms를 늘리면 어떤 변화가 생기나요?

### 응용
1. 주문/로그 토픽 각각의 튜닝안을 작성해보세요.
2. 튜닝 검증 지표를 정의해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
