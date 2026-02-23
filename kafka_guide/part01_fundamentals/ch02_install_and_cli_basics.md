# Chapter 02 - 설치와 CLI 기초

- 상태: 초안 완료
- 목표 분량: 10쪽

## 학습 목표
- 로컬 Kafka 실습 환경을 구축할 수 있다.
- Topic 생성/조회 기본 CLI를 사용할 수 있다.
- Producer/Consumer 콘솔 실습을 수행할 수 있다.

## 핵심 개념

환경 재현성은 Kafka 학습의 시작점입니다.
CLI로 토픽/생산/소비를 직접 확인하면
추상 개념(파티션/오프셋/그룹)이 빠르게 잡힙니다.

## 실습 예제

```bash
docker compose -f notification_platform/docker-compose.idea3.yml up -d

docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic quickstart --partitions 1 --replication-factor 1

docker exec -it idea3-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic quickstart
```

별도 터미널:

```bash
docker exec -it idea3-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic quickstart --from-beginning
```

## 설계 포인트
- 초반엔 파티션 1개로 기본 흐름부터 익힌다.
- 운영 토픽 생성은 자동화 절차(IaC/스크립트)로 관리한다.
- 삭제/보관 정책을 토픽 생성 시 함께 점검한다.

## 자주 하는 실수
1. bootstrap-server/리스너 주소 혼동
2. producer/consumer 블로킹 동작 오해
3. 토픽 정책 확인 없이 기본값 사용

## 요약
- CLI 실습은 Kafka 개념을 가장 빠르게 체화하는 방법이다.
- 토픽 생성-생산-소비-조회 루프를 먼저 익혀야 한다.

## 연습문제
### 기초
1. 토픽 생성 후 describe 결과를 해석해보세요.
2. 메시지 5건을 생산/소비해보세요.

### 응용
1. 파티션 3개 토픽으로 변경해 차이를 기록해보세요.
2. 운영 금지 명령 목록을 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [ ] 최종 교정 완료
