# Kafka 가이드 인덱스

이 문서는 `kafka_guide` 전체를 책처럼 탐색하기 위한 목차입니다.

## 시작하기

- [README](README.md)
- [서문](00_preface.md)
- [이 책을 읽는 방법](01_how_to_read.md)
- [집필 계획 및 진행표](WRITING_PLAN_TODO.md)
- [한국어 집필 스타일 가이드](STYLE_KR.md)
- [검증 로그](VERIFICATION_LOG.md)

## Part 01 - 기초 개념

- [Chapter 01 - Kafka란 무엇이고, 왜 필요한가](part01_fundamentals/ch01_kafka_what_and_why.md)
- [Chapter 02 - 설치와 CLI 기초](part01_fundamentals/ch02_install_and_cli_basics.md)
- [Chapter 03 - Broker/Topic/Partition 개요](part01_fundamentals/ch03_broker_topic_partition_overview.md)
- [Chapter 04 - 메시지 모델과 전달 보장](part01_fundamentals/ch04_message_model_and_delivery_semantics.md)
- [Chapter 05 - Retention과 Compaction](part01_fundamentals/ch05_retention_and_compaction.md)

## Part 02 - 핵심 구성 요소

- [Chapter 06 - Topic/Partition 설계](part02_core_concepts/ch06_topic_and_partition_design.md)
- [Chapter 07 - Producer 기초](part02_core_concepts/ch07_producer_basics.md)
- [Chapter 08 - Consumer/Group 기초](part02_core_concepts/ch08_consumer_and_group_basics.md)
- [Chapter 09 - Offset 커밋과 Rebalance](part02_core_concepts/ch09_offset_commit_and_rebalance.md)
- [Chapter 10 - Key 정렬성과 Idempotency](part02_core_concepts/ch10_key_ordering_and_idempotency.md)
- [Chapter 11 - 스키마/직렬화 입문](part02_core_concepts/ch11_schema_registry_and_serialization_intro.md)

## Part 03 - 명령/패턴

- [Chapter 12 - Producer acks/retries/batching](part03_commands_patterns/ch12_producer_acks_retries_batching.md)
- [Chapter 13 - Exactly-Once와 트랜잭션](part03_commands_patterns/ch13_exactly_once_transactions.md)
- [Chapter 14 - DLQ/재시도/에러 처리](part03_commands_patterns/ch14_dlq_retry_and_error_handling.md)
- [Chapter 15 - Outbox와 이벤트 패턴](part03_commands_patterns/ch15_outbox_and_event_driven_patterns.md)
- [Chapter 16 - CLI 운영/관리 패턴](part03_commands_patterns/ch16_cli_admin_patterns.md)

## Part 04 - 아키텍처/운영

- [Chapter 17 - Replication/ISR/min.insync.replicas](part04_architecture_ops/ch17_replication_isr_min_insync.md)
- [Chapter 18 - KRaft와 Controller Failover](part04_architecture_ops/ch18_kraft_and_controller_failover.md)
- [Chapter 19 - 클러스터 확장과 파티션 재배치](part04_architecture_ops/ch19_cluster_scaling_and_partition_reassignment.md)
- [Chapter 20 - 모니터링: Lag/Latency/Throughput](part04_architecture_ops/ch20_monitoring_lag_latency_throughput.md)
- [Chapter 21 - 백업/복구/마이그레이션](part04_architecture_ops/ch21_backup_restore_and_migration.md)

## Part 05 - 성능/보안

- [Chapter 22 - 성능 튜닝 플레이북](part05_performance_security/ch22_performance_tuning_playbook.md)
- [Chapter 23 - Consumer Lag 감소 전략](part05_performance_security/ch23_consumer_lag_reduction_strategies.md)
- [Chapter 24 - Hot Partition과 트래픽 스큐](part05_performance_security/ch24_hot_partition_and_skew.md)
- [Chapter 25 - 보안: ACL/TLS/SASL](part05_performance_security/ch25_security_acl_tls_sasl.md)
- [Chapter 26 - 용량 계획과 비용](part05_performance_security/ch26_capacity_planning_and_cost.md)

## Part 06 - 프로젝트 적용

- [Chapter 27 - Spring Boot Kafka 기초](part06_project_integration/ch27_spring_boot_kafka_basics.md)
- [Chapter 28 - 이벤트 계약과 버전 관리](part06_project_integration/ch28_event_contract_versioning.md)
- [Chapter 29 - 장애 대응/복구 런북](part06_project_integration/ch29_failure_recovery_runbook.md)
- [Chapter 30 - 사례 연구: Notification Platform의 Kafka](part06_project_integration/ch30_case_study_notification_platform.md)

## 부록

- [Appendix A - 용어집](appendices/a_glossary.md)
- [Appendix B - 명령어 치트시트](appendices/b_command_cheatsheet.md)
- [Appendix C - 연습문제 정답 가이드](appendices/c_quiz_answer.md)

## 추천 학습 순서

1. 서문 -> 읽는 방법 -> Part 01
2. Part 02 -> Part 03
3. Part 04 -> Part 05
4. Part 06 -> 부록
