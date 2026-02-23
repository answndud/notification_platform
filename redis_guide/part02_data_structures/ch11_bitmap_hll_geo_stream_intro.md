# Chapter 11 - Bitmap, HyperLogLog, Geo, Stream 입문

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표

- 특수 자료구조의 사용 목적을 구분할 수 있다.
- Bitmap/HLL/Geo/Stream의 대표 명령을 이해한다.
- "언제 쓰고 언제 피해야 하는지" 기준을 세울 수 있다.

## 핵심 개념

Redis에는 기본 자료형 외에도 특정 문제를 효율적으로 푸는 구조가 있습니다.

이 자료구조들은 "특정 목적 최적화"라는 공통점이 있습니다.
즉, 문제에 맞으면 강력하지만,
용도를 벗어나면 오히려 복잡도만 늘어납니다.

### Bitmap

- 비트 단위 저장
- 출석 체크, 플래그 집계에 유리

### HyperLogLog

- 고유값 개수를 근사치로 계산
- 큰 집합의 cardinality 추정에 유리

### Geo

- 위도/경도 기반 근접 조회

### Stream

- 이벤트 로그형 자료구조
- 소비 그룹을 통한 메시지 처리 가능

## 실습 예제

```bash
# Bitmap
redis-cli -p 6380 SETBIT attend:2026-02-23 1001 1
redis-cli -p 6380 BITCOUNT attend:2026-02-23

# HyperLogLog
redis-cli -p 6380 PFADD uv:home user1 user2 user2
redis-cli -p 6380 PFCOUNT uv:home
```

```bash
# Geo
redis-cli -p 6380 GEOADD store:loc 127.0276 37.4979 gangnam
redis-cli -p 6380 GEOSEARCH store:loc FROMLONLAT 127.03 37.50 BYRADIUS 3 km
```

## 설계 포인트

- 정확한 개수 필요 -> HLL 부적합
- 간단 큐 이상 보장 필요 -> Stream 또는 외부 브로커 비교
- 지리 검색은 정밀 GIS 기능 한계를 인식하고 사용

판단 기준 요약:

- 정확도 우선인가
- 처리 보장 우선인가
- 운영 복잡도를 감당할 수 있는가

## 자주 하는 실수

1. 근사치 구조(HLL)를 정밀 집계에 사용
2. Stream 보장 모델을 Kafka와 동일하게 가정
3. Geo로 복잡한 공간 분석까지 해결하려고 시도

## 요약

- 특수 자료구조는 문제 유형이 맞을 때 압도적으로 효율적이다.
- 자료구조 선택 전 정확도, 보장, 확장 요구를 먼저 확인해야 한다.

## 연습문제

### 기초

1. HLL로 일간 방문자 수 근사 집계를 구성해보세요.
2. Bitmap으로 출석 여부를 표현해보세요.

### 응용

1. Stream과 Kafka 선택 기준을 표로 정리해보세요.
2. Geo 기능으로 "반경 2km 매장 조회" 설계를 작성해보세요.

## 챕터 체크리스트

- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
