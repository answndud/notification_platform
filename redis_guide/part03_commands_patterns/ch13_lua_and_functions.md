# Chapter 13 - Lua와 Functions

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표

- Lua 스크립트 도입 목적을 이해할 수 있다.
- 원자적 복합 로직을 스크립트로 표현할 수 있다.
- 스크립트 운영 시 버전/배포 주의점을 설명할 수 있다.

## 핵심 개념

Lua 스크립트는 여러 Redis 명령을 서버 내부에서 한 번에 실행합니다.
네트워크 왕복을 줄이고, 복합 작업의 원자성을 높일 수 있습니다.

실무에서 Lua는 "복잡한 트랜잭션 대체"라기보다
"짧은 원자 연산 캡슐"로 사용하는 것이 안전합니다.
스크립트가 길어질수록 테스트/운영 부담이 빠르게 커집니다.

기본 명령:

- `EVAL`
- `EVALSHA`

Redis 7에서는 Functions로 재사용 가능한 서버 함수 관리도 가능합니다.
Functions는 스크립트 조각을 반복 전송하지 않고,
서버에 라이브러리 형태로 등록해 호출할 수 있다는 점이 핵심입니다.

## 실습 예제

```bash
redis-cli -p 6380 EVAL "return redis.call('INCR', KEYS[1])" 1 metric:api:count
```

조건부 갱신 예시:

```bash
redis-cli -p 6380 EVAL "if redis.call('EXISTS', KEYS[1]) == 1 then return redis.call('INCRBY', KEYS[1], ARGV[1]) else return -1 end" 1 stock:item:1 3
```

Functions 예시(Redis 7+):

```bash
redis-cli -p 6380 FUNCTION LOAD "#!lua name=libmath\nredis.register_function('incrby_safe', function(keys, args)\n  if redis.call('EXISTS', keys[1]) == 0 then return -1 end\n  return redis.call('INCRBY', keys[1], tonumber(args[1]))\nend)"
redis-cli -p 6380 SET stock:item:2 10
redis-cli -p 6380 FCALL incrby_safe 1 stock:item:2 3
redis-cli -p 6380 FCALL incrby_safe 1 stock:item:404 3
```

관찰 포인트:

- 서버에 등록된 함수는 `FCALL`로 재사용됩니다.
- 반환 규약(예: `-1`)을 통일하면 애플리케이션 예외 처리가 쉬워집니다.

## 설계 포인트

- 여러 명령이 논리적으로 하나라면 스크립트 검토
- 스크립트는 짧고 예측 가능하게 유지
- 실패 코드와 반환 스키마를 표준화

운영 권장사항:

- 스크립트별 버전과 배포 이력 관리
- 시간 오래 걸리는 루프/대량 순회 로직 금지
- 반환값을 숫자 코드 + 메시지 규약으로 통일

## 자주 하는 실수

1. 스크립트에 과도한 비즈니스 로직 삽입
2. 반환값 규약 없이 코드마다 다르게 사용
3. 버전 관리 없이 운영 반영

## 요약

- Lua/Functions는 원자적 복합 처리와 RTT 절감에 유리하다.
- 스크립트 품질은 단순성, 버전 관리, 반환 규약이 좌우한다.

## 연습문제

### 기초

1. 카운터 증가 Lua 스크립트를 작성해보세요.
2. 조건부 키 생성 스크립트를 작성해보세요.

### 응용

1. 재고 차감 + 부족 시 실패 스크립트 인터페이스를 설계해보세요.
2. 스크립트 배포 버전 전략(v1/v2 공존)을 정의해보세요.

## 챕터 체크리스트

- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
