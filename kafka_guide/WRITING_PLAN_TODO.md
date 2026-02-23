# Kafka 가이드 집필 계획 (300쪽 목표)

## 진행 보드

- [x] Phase 1: 범위/문체/구조 확정
- [x] Phase 2: Part 01~02 초안 완성
- [x] Phase 3: Part 03~04 초안 완성
- [x] Phase 4: Part 05~06 초안 완성
- [x] Phase 5: 부록/용어집/최종 교정

## 검증 메모

- 2026-02-23: Kafka CLI 실실행 검증 1차 완료(기초/핵심/운영 일부)
- 2026-02-23: Kafka CLI 실실행 검증 2차 완료(DLQ/lag/skew/producer 속성)
- 2026-02-23: 보강 챕터 검증 3차 시도(ACL/EOS/런북/사례) - 제약 사항 로그 반영
- 2026-02-23: 보강 챕터 검증 4차 완료(`ch13`, `ch25`) - 검증 스크립트 추가
- 2026-02-23: Part 05~06 추가 검증 완료(`ch26`, `ch27`, `ch28`) - 계산/테스트/호환성 스크립트 반영
- 2026-02-23: 미검증 챕터 검증 6차 완료(`ch11`, `ch15`, `ch18`, `ch19`, `ch21`, `ch22`, `ch29`, `ch30`) - 실습/시뮬레이션 스크립트 반영
- 검증 완료 챕터: `ch01`~`ch30`
- 보류 챕터: 없음
- 상세 로그: `kafka_guide/VERIFICATION_LOG.md`

## 가독성 보강 메모

- 2026-02-23: 초보자 가독성 보강 1차 반영(머메이드/ASCII 다이어그램)
- 대상: `ch03`, `ch04`, `ch06`, `ch08`, `ch09`, `ch10`, `ch11`, `ch12`, `ch13`, `ch14`, `ch15`, `ch16`

## 집필 순서 (권장)

1. Part 01 기초 개념
2. Part 02 핵심 구성 요소
3. Part 03 명령/패턴
4. Part 04 아키텍처/운영
5. Part 05 성능/보안
6. Part 06 프로젝트 적용
7. 부록

## 분량 예산

- Part 01: 50쪽
- Part 02: 65쪽
- Part 03: 55쪽
- Part 04: 55쪽
- Part 05: 45쪽
- Part 06: 25쪽
- 부록: 5쪽
- 총합: 300쪽

## 챕터별 TODO 체크리스트

- [x] 01 Chapter 01 - Kafka란 무엇이고, 왜 필요한가
- [x] 02 Chapter 02 - 설치와 CLI 기초
- [x] 03 Chapter 03 - Broker/Topic/Partition 개요
- [x] 04 Chapter 04 - 메시지 모델과 전달 보장
- [x] 05 Chapter 05 - Retention과 Compaction
- [x] 06 Chapter 06 - Topic/Partition 설계
- [x] 07 Chapter 07 - Producer 기초
- [x] 08 Chapter 08 - Consumer/Group 기초
- [x] 09 Chapter 09 - Offset 커밋과 Rebalance
- [x] 10 Chapter 10 - Key 정렬성과 Idempotency
- [x] 11 Chapter 11 - 스키마/직렬화 입문
- [x] 12 Chapter 12 - Producer acks/retries/batching
- [x] 13 Chapter 13 - Exactly-Once와 트랜잭션
- [x] 14 Chapter 14 - DLQ/재시도/에러 처리
- [x] 15 Chapter 15 - Outbox와 이벤트 패턴
- [x] 16 Chapter 16 - CLI 운영/관리 패턴
- [x] 17 Chapter 17 - Replication/ISR/min.insync.replicas
- [x] 18 Chapter 18 - KRaft와 Controller Failover
- [x] 19 Chapter 19 - 클러스터 확장과 파티션 재배치
- [x] 20 Chapter 20 - 모니터링: Lag/Latency/Throughput
- [x] 21 Chapter 21 - 백업/복구/마이그레이션
- [x] 22 Chapter 22 - 성능 튜닝 플레이북
- [x] 23 Chapter 23 - Consumer Lag 감소 전략
- [x] 24 Chapter 24 - Hot Partition과 트래픽 스큐
- [x] 25 Chapter 25 - 보안: ACL/TLS/SASL
- [x] 26 Chapter 26 - 용량 계획과 비용
- [x] 27 Chapter 27 - Spring Boot Kafka 기초
- [x] 28 Chapter 28 - 이벤트 계약과 버전 관리
- [x] 29 Chapter 29 - 장애 대응/복구 런북
- [x] 30 Chapter 30 - 사례 연구: Notification Platform의 Kafka
- [x] 31 부록 A 용어집
- [x] 32 부록 B 명령어 치트시트
- [x] 33 부록 C 연습문제 정답

## 완료 기준 (챕터 단위)

- [x] 초보자도 이해 가능한 학습 목표 제시
- [x] CLI 예제 1개 이상 + 실무 시나리오 1개 이상 포함
- [x] 자주 하는 실수/반례 섹션 포함
- [x] 요약/연습문제 포함
- [x] 용어 일관성 및 한국어 문장 교정 완료
