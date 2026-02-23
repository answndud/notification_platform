# Chapter 20 - 모니터링: Lag/Latency/Throughput

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- Kafka 핵심 운영 지표를 설명할 수 있다.
- lag 급등 원인을 분석할 수 있다.
- 알림 임계치와 대응 절차를 수립할 수 있다.

## 핵심 개념

Kafka 운영의 핵심 지표:
- Consumer lag
- Produce/consume throughput
- End-to-end latency
- Under-replicated partitions
- Request error rate

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group payment-workers
```

## 설계 포인트
- 평균보다 p95/p99 지표를 우선 본다.
- lag 임계치 초과 시 자동 알림+runbook 연결.
- 원인 분석 순서를 표준화(생산 증가/소비 저하/외부 장애).

## 자주 하는 실수
1. lag만 보고 원인 분해를 안 함
2. 경보는 있는데 대응 절차가 없음
3. 지표 보관 기간이 짧아 추세 분석 불가

## 요약
- 모니터링은 관측이 아니라 의사결정 시스템이다.
- 지표와 대응 절차를 항상 같이 설계해야 한다.

## 연습문제
### 기초
1. 필수 지표 5개를 선정해보세요.
2. lag 급등 시 점검 순서를 적어보세요.

### 응용
1. 알림 임계치 정책을 설계해보세요.
2. 지연 분석 런북을 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
