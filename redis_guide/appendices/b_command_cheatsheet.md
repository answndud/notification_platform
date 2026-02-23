# Appendix B - 명령어 치트시트

- 상태: 초안 완료
- 목표 분량: 2쪽

## 기본

- `SET key value [EX seconds]`
- `GET key`
- `DEL key`
- `EXPIRE key seconds`
- `TTL key`

## String

- `INCR key`
- `DECR key`
- `MSET k1 v1 k2 v2`
- `MGET k1 k2`

## Hash

- `HSET key field value`
- `HGET key field`
- `HGETALL key`

## List

- `LPUSH key value`
- `RPUSH key value`
- `LPOP key`
- `LRANGE key 0 -1`

## Set / Sorted Set

- `SADD key member`
- `SISMEMBER key member`
- `ZADD key score member`
- `ZREVRANGE key 0 9 WITHSCORES`

## 운영

- `INFO`
- `INFO memory`
- `INFO persistence`
- `SLOWLOG GET 10`
- `SCAN 0 MATCH pattern COUNT 100`

## 주의 명령

- `KEYS *` (운영 환경 사용 금지 권장)
- `FLUSHDB`, `FLUSHALL` (파괴적 명령)
