# Chapter 02 - 설치와 CLI 기초

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표

- 로컬 Redis 실습 환경을 구축할 수 있다.
- `redis-cli`로 기본 쓰기/조회/만료 확인을 수행할 수 있다.
- 운영 사고를 줄이는 안전한 명령 사용 습관을 익힐 수 있다.

## 왜 이 장이 중요한가

Redis 실수의 절반은 "환경 오해"에서 시작합니다.
로컬과 운영 환경의 가정이 다르면 TTL 동작, 데이터 유지, 메모리 사용량을 잘못 해석하게 됩니다.

따라서 먼저 재현 가능한 실습 환경을 만들고,
CLI를 통해 기본 동작을 몸으로 익히는 것이 중요합니다.

## 설치 방식 개요

Redis를 시작하는 방법은 보통 세 가지입니다.

1. 네이티브 설치(brew/apt)
- 빠르게 단일 실험을 할 때 유리합니다.

2. Docker 단일 컨테이너
- 팀 내 동일 환경을 맞출 때 유리합니다.

3. Docker Compose 통합 실행
- DB, 메시지 브로커와 함께 통합 실습할 때 유리합니다.

이 레포에서는 3번 방식이 이미 준비되어 있습니다.

## 프로젝트 기반 실행(이 레포 권장)

Redis 서비스 정의 위치:
`notification_platform/docker-compose.idea3.yml:19`

인프라 실행:

```bash
docker compose -f notification_platform/docker-compose.idea3.yml up -d
```

이 레포의 Redis 매핑:

- 컨테이너 이름: `idea3-redis`
- 이미지: `redis:7`
- 호스트 포트: `6380`
- 컨테이너 포트: `6379`
- 영속성 옵션: AOF 사용(`--appendonly yes`)

상태 확인:

```bash
docker ps --filter name=idea3-redis
docker exec -it idea3-redis redis-cli PING
```

정상 응답: `PONG`

## 첫 redis-cli 세션

호스트에서 접속:

```bash
redis-cli -p 6380
```

기본 명령 실행:

```text
SET hello redis
GET hello
EXPIRE hello 60
TTL hello
DEL hello
```

해석 포인트:
- `SET`: 값 저장
- `GET`: 값 조회
- `EXPIRE`: 만료 시간 설정
- `TTL`: 남은 만료 시간 확인
- `DEL`: 키 삭제

## 자료형을 항상 확인하는 습관

Redis 키는 자료형을 가집니다.
같은 키를 String으로 썼다가 Hash로 바꾸면 런타임 오류의 원인이 됩니다.

```text
SET sample:key "1"
TYPE sample:key
DEL sample:key
HSET sample:key field value
TYPE sample:key
```

실무에서는 `TYPE` 확인만으로도 디버깅 시간을 크게 줄일 수 있습니다.

## 안전한 명령어 사용 습관

### 규칙 1: 위험한 전역 명령은 기본 금지

운영 환경에서 아래 명령은 엄격히 제한해야 합니다.

- `FLUSHALL`
- `FLUSHDB`
- `KEYS *`

특히 `KEYS`는 키가 많을 때 서버 처리 루프를 오래 점유할 수 있습니다.
조회 목적이면 `SCAN` 계열 명령을 사용하세요.

### 규칙 2: 임시 데이터는 반드시 TTL을 붙인다

세션/토큰/캐시 키에 TTL이 없으면 메모리 누수와 비슷한 현상이 발생합니다.

권장 패턴:

```text
SET session:user:1001 token EX 1800
```

### 규칙 3: 키 네이밍 규칙을 먼저 정한다

권장 예시:

- `cache:product:123`
- `auth:refresh:user:1001`
- `rate:login:ip:1.2.3.4`

네이밍 규칙은 가독성과 장애 대응 속도를 동시에 높입니다.

## 최소 트러블슈팅 체크리스트

명령 결과가 이상하면 아래 순서로 확인하세요.

1. 접속 대상 확인
- 올바른 호스트/포트에 연결했는가?

2. 키 존재/자료형 확인
- `EXISTS key`, `TYPE key`

3. 만료 상태 확인
- `TTL key` 또는 `PTTL key`

4. 데이터 소실 원인 확인
- 만료, 재시작, 플러시, 다른 프로세스 삭제 여부

## 자주 하는 실수

1. 기본 포트 `6379`만 가정한다.
- 이 레포에서는 호스트 포트가 `6380`입니다.

2. TTL 없는 임시 키를 남긴다.
- 시간이 지나며 메모리가 조용히 증가합니다.

3. `KEYS *`를 조회 도구처럼 쓴다.
- 데이터가 많아질수록 운영 리스크가 커집니다.

4. 키 이름 규칙 없이 개발한다.
- 충돌, 중복, 추적 어려움이 빠르게 늘어납니다.

## 요약

- Redis 학습의 시작은 재현 가능한 로컬 환경 구성이다.
- 이 레포에서는 compose 기반 Redis(`6380`)로 실습하면 된다.
- `SET/GET/EXPIRE/TTL/DEL/TYPE`을 먼저 정확히 익힌다.
- 위험 명령 회피, TTL 강제, 키 규칙 표준화가 운영 안정성의 기본이다.

## 연습문제

### 기초

1. compose로 Redis를 띄우고 `PING -> PONG`를 확인해보세요.
2. TTL 30초 키를 만들고 5초 간격으로 `TTL` 변화를 관찰해보세요.

### 응용

1. 세션/토큰/레이트 리밋용 키 네이밍 규칙을 직접 설계해보세요.
2. 운영 금지 명령 목록과 예외 승인 절차를 1페이지로 작성해보세요.

## 챕터 체크리스트

- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
