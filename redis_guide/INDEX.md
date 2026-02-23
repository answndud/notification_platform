# Redis 가이드 인덱스

이 문서는 `redis_guide` 전체를 책처럼 탐색하기 위한 목차입니다.

## 시작하기

- [README](README.md)
- [서문](00_preface.md)
- [이 책을 읽는 방법](01_how_to_read.md)
- [집필 계획 및 진행표](WRITING_PLAN_TODO.md)
- [한국어 집필 스타일 가이드](STYLE_KR.md)
- [챕터 템플릿](CHAPTER_TEMPLATE.md)
- [검증 로그](VERIFICATION_LOG.md)

## Part 01 - 기초 개념

- [Chapter 01 - Redis란 무엇이고, 왜 필요한가](part01_fundamentals/ch01_redis_what_and_why.md)
- [Chapter 02 - 설치와 CLI 기초](part01_fundamentals/ch02_install_and_cli_basics.md)
- [Chapter 03 - 메모리 모델과 영속성 개요](part01_fundamentals/ch03_memory_model_and_persistence_overview.md)
- [Chapter 04 - 키 설계 원칙](part01_fundamentals/ch04_key_design_principles.md)
- [Chapter 05 - 만료 정책과 TTL](part01_fundamentals/ch05_expiration_and_ttl.md)

## Part 02 - 자료구조

- [Chapter 06 - String 자료형](part02_data_structures/ch06_string.md)
- [Chapter 07 - List 자료형](part02_data_structures/ch07_list.md)
- [Chapter 08 - Set 자료형](part02_data_structures/ch08_set.md)
- [Chapter 09 - Sorted Set 자료형](part02_data_structures/ch09_sorted_set.md)
- [Chapter 10 - Hash 자료형](part02_data_structures/ch10_hash.md)
- [Chapter 11 - Bitmap, HyperLogLog, Geo, Stream 입문](part02_data_structures/ch11_bitmap_hll_geo_stream_intro.md)

## Part 03 - 명령/패턴

- [Chapter 12 - 원자성과 트랜잭션](part03_commands_patterns/ch12_atomicity_and_transactions.md)
- [Chapter 13 - Lua와 Functions](part03_commands_patterns/ch13_lua_and_functions.md)
- [Chapter 14 - Pub/Sub와 Streams](part03_commands_patterns/ch14_pubsub_and_streams.md)
- [Chapter 15 - SCAN vs KEYS, BigKey 대응](part03_commands_patterns/ch15_scan_vs_keys_and_bigkey.md)
- [Chapter 16 - 파이프라이닝과 배치 처리](part03_commands_patterns/ch16_pipelining_and_batch.md)

## Part 04 - 아키텍처/운영

- [Chapter 17 - RDB/AOF와 내구성 설계](part04_architecture_ops/ch17_rdb_aof_and_durability.md)
- [Chapter 18 - 복제와 Sentinel](part04_architecture_ops/ch18_replication_and_sentinel.md)
- [Chapter 19 - Cluster, 샤딩, 해시 슬롯](part04_architecture_ops/ch19_cluster_sharding_slot.md)
- [Chapter 20 - 모니터링, Slowlog, 지연 분석](part04_architecture_ops/ch20_monitoring_slowlog_latency.md)
- [Chapter 21 - 백업, 복구, 마이그레이션](part04_architecture_ops/ch21_backup_restore_and_migration.md)

## Part 05 - 성능/보안

- [Chapter 22 - 캐시 패턴](part05_performance_security/ch22_cache_patterns.md)
- [Chapter 23 - 캐시 무효화 전략](part05_performance_security/ch23_invalidation_strategies.md)
- [Chapter 24 - HotKey와 Thundering Herd](part05_performance_security/ch24_hotkey_thundering_herd.md)
- [Chapter 25 - 보안: ACL, TLS, 네트워크](part05_performance_security/ch25_security_acl_tls_network.md)
- [Chapter 26 - 용량 계획과 비용](part05_performance_security/ch26_capacity_planning_cost.md)

## Part 06 - 프로젝트 적용

- [Chapter 27 - Spring Boot Redis 기초](part06_project_integration/ch27_spring_boot_redis_basics.md)
- [Chapter 28 - 토큰/세션 설계](part06_project_integration/ch28_token_session_design.md)
- [Chapter 29 - 장애 대응/복구 런북](part06_project_integration/ch29_failure_recovery_runbook.md)
- [Chapter 30 - 사례 연구: Notification Platform](part06_project_integration/ch30_case_study_notification_platform.md)

## 부록

- [Appendix A - 용어집](appendices/a_glossary.md)
- [Appendix B - 명령어 치트시트](appendices/b_command_cheatsheet.md)
- [Appendix C - 연습문제 정답 가이드](appendices/c_quiz_answer.md)

## 추천 학습 순서

1. 서문 -> 읽는 방법 -> Part 01
2. Part 02 -> Part 03
3. Part 04 -> Part 05
4. Part 06 -> 부록
