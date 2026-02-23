# Chapter 10 - Hash 자료형

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표

- Hash를 객체 필드 저장 관점으로 이해한다.
- 부분 갱신 시 String 대비 장점을 설명할 수 있다.
- Hash 사용 시 필드 설계 원칙을 적용할 수 있다.

## 핵심 개념

Hash는 key 내부에 field-value 쌍을 저장하는 자료형입니다.
한 사용자 정보처럼 "속성이 여러 개인 객체"에 적합합니다.

String에 JSON을 통째로 넣는 방식과 비교하면,
Hash는 필드 단위 갱신이 가능해 쓰기 비용을 줄일 수 있습니다.
특히 일부 필드만 자주 바뀌는 도메인에서 효과가 큽니다.

주요 명령:

- `HSET`, `HGET`, `HMGET`
- `HGETALL`, `HDEL`
- `HINCRBY`

## 실습 예제

```bash
redis-cli -p 6380 DEL user:profile:1
redis-cli -p 6380 HSET user:profile:1 name kim age 33 tier gold
redis-cli -p 6380 HGET user:profile:1 name
redis-cli -p 6380 HINCRBY user:profile:1 age 1
redis-cli -p 6380 HGETALL user:profile:1
```

## 설계 포인트

- 필드 단위 갱신이 많으면 Hash가 효율적
- 너무 많은 필드를 한 키에 몰아넣지 않기
- 전체 조회(`HGETALL`) 남발을 피하고 필요한 필드만 조회

추가 팁:

- 필드 이름 표준(예: `createdAt`, `updatedAt`)을 팀 규칙으로 고정
- 스키마 변경 시 신규 필드 기본값 전략을 사전에 정의

## 자주 하는 실수

1. Hash인데도 매번 전체 필드를 읽고 씀
2. 필드 네이밍 규칙 없이 확장해 혼란 발생
3. TTL 없이 객체를 무기한 보관

## 요약

- Hash는 객체형 데이터의 부분 갱신에 강하다.
- 필드 설계와 조회 범위 제어가 중요하다.
- 메모리/성능 균형을 위해 키-필드 구조를 표준화해야 한다.

## 연습문제

### 기초

1. 사용자 프로필 Hash를 만들고 필드 3개를 저장해보세요.
2. `HINCRBY`로 카운터 필드를 증가시켜보세요.

### 응용

1. 주문 상태 객체를 Hash로 모델링해보세요.
2. String JSON vs Hash 선택 기준을 표로 정리해보세요.

## 챕터 체크리스트

- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
