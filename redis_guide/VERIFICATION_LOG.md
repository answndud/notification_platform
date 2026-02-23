# Redis Guide 검증 로그

## 2026-02-23 1차 검증 시도

목표:

- 가이드북 챕터 예제 명령(`redis-cli`) 실제 실행 검증

시도 결과:

- Docker Redis 실행 실패
  - 명령: `docker compose -f notification_platform/docker-compose.idea3.yml up -d redis`
  - 오류: Docker daemon 연결 불가(`Cannot connect to the Docker daemon`)
- 로컬 바이너리 미설치
  - `redis-server not found`
  - `redis-cli not found`

판단:

- 현재 환경에서는 예제 명령 실실행 검증을 진행할 수 없음
- 각 챕터의 `예제 명령어 검증 완료` 체크는 보류 상태 유지

재개 조건:

1. Docker daemon 정상 기동 또는
2. 로컬 Redis(`redis-server`, `redis-cli`) 설치

재개 시 권장 검증 순서:

1. 기본 명령: `SET/GET/EXPIRE/TTL/DEL`
2. 자료형: `String/List/Set/Sorted Set/Hash`
3. 운영 명령: `SCAN`, `SLOWLOG`, `INFO persistence`
4. 특수 구조: `PFADD/PFCOUNT`, `GEOADD/GEOSEARCH`, `XADD/XRANGE`

## 2026-02-23 2차 검증 결과 (Docker Desktop 기동 후)

실행 환경:

- `docker compose -f notification_platform/docker-compose.idea3.yml up -d` 성공
- Redis 컨테이너(`idea3-redis`) 기준으로 `redis-cli` 명령 검증

검증 완료 항목:

1. 기본/TTL
- `PING`, `SET`, `GET`, `EXPIRE`, `TTL`, `DEL`

2. 키/영속성/스캔
- `INFO persistence`, `SCAN ... MATCH ... COUNT ...`

3. 자료형
- String: `INCR`
- List: `RPUSH`, `LRANGE`, `LPOP`
- Set: `SADD`, `SMEMBERS`, `SISMEMBER`
- Sorted Set: `ZADD`, `ZREVRANGE`, `ZREVRANK`
- Hash: `HSET`, `HGET`, `HINCRBY`, `HGETALL`

4. 특수 구조
- Bitmap: `SETBIT`, `BITCOUNT`
- HyperLogLog: `PFADD`, `PFCOUNT`
- Geo: `GEOADD`, `GEOSEARCH`
- Stream: `XADD`, `XRANGE`, `XGROUP CREATE`, `XREADGROUP`

5. 고급/운영 명령
- 트랜잭션: `MULTI`, `EXEC`
- 스크립트: `EVAL`
- 진단: `--bigkeys`, `CONFIG GET appendonly`, `CONFIG GET appendfsync`, `SLOWLOG LEN`
- 파이프라이닝: `redis-cli --pipe` (200건 주입)

체크 반영:

- 아래 챕터의 `예제 명령어 검증 완료`를 `[x]`로 반영
  - `ch01`~`ch17` (단, `ch18`, `ch19` 제외)
  - `ch20`

검증 보류 항목:

- `ch18` 복제/Sentinel: 단일 Redis 컨테이너 구성으로 Sentinel/다중 노드 failover 검증 불가
- `ch19` Cluster/해시 슬롯: Cluster 토폴로지 부재로 슬롯/크로스슬롯 검증 불가
- `ch21`~`ch30`: 운영 절차/아키텍처 중심으로, 별도 앱/멀티노드/통합 시나리오 준비 후 심화 검증 권장
