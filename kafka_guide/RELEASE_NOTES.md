# Kafka Guide 릴리즈 노트 (최종 교정본)

작성일: 2026-02-23

## 이번 릴리즈 핵심

- `kafka_guide` 전 챕터(`ch01`~`ch30`) 최종 교정 완료
- 전 챕터 체크리스트의 `최종 교정 완료` 항목 반영
- 예제 명령어 검증 시나리오를 챕터별로 표준화

## 큰 변경 사항

1. 문서 품질 고도화
- 학습 목표/실습 목적/실패 점검/운영 체크리스트 구조를 강화
- 운영 관점(알림, 복구, 런북, 보안, 비용) 설명 밀도 보강

2. 보강 챕터 심화
- `ch13`, `ch20`, `ch25`, `ch29`, `ch30` 중심으로 실무 시나리오 확장
- `ch22`, `ch26`, `ch28`에 검증 가능한 실습 흐름 추가

3. 검증 자동화 추가
- Kafka 가이드 검증용 스크립트 다수 추가(`scripts/verify_kafka_*.sh`)
- 보안 검증용 Kafka 구성(`docker-compose.kafka-sec.yml`) 및
  Schema Registry 검증 구성(`docker-compose.kafka-sr.yml`) 추가

## 검증 상태 요약

- `VERIFICATION_LOG.md` 기준 `ch01`~`ch30` 검증 반영 완료
- 멀티 브로커/운영 리허설이 필요한 챕터는 시뮬레이션/드릴 기반으로 보강

## 학습자 관점 개선 효과

- "개념 설명"에서 "실행 가능한 절차"로 전환
- 실패 시 점검 경로가 명확해져 초보자 재현성 향상
- 실무 적용(보안/운영/장애/비용) 의사결정 기준 강화

## 참고 파일

- `kafka_guide/WRITING_PLAN_TODO.md`
- `kafka_guide/VERIFICATION_LOG.md`
- `scripts/verify_kafka_eos.sh`
- `scripts/verify_kafka_acl.sh`
- `scripts/verify_kafka_schema_registry.sh`
