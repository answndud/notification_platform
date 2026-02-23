# Chapter 16 - CLI 운영/관리 패턴

- 상태: 초안 완료
- 목표 분량: 11쪽

## 학습 목표
- Kafka 운영 필수 CLI를 사용할 수 있다.
- 토픽/그룹/설정 점검 루틴을 만들 수 있다.
- 운영 위험 작업의 승인 절차를 정의할 수 있다.

## 핵심 개념

CLI는 장애 대응 속도를 크게 좌우합니다.
평소 점검 루틴이 없으면 장애 중 잘못된 명령으로 피해가 커질 수 있습니다.

## 직관 구분

```text
읽기 전용 점검 명령:
  - kafka-topics --list/--describe
  - kafka-consumer-groups --describe

변경성 명령:
  - kafka-configs --alter
  - offset reset
  - topic delete
```

초보자에게 중요한 원칙:
- 장애 중에는 "읽기 명령"부터 실행하고,
- 변경 명령은 승인/기록 후 실행합니다.

## 실습 예제

```bash
docker exec -it idea3-kafka kafka-topics --bootstrap-server localhost:9092 --list
docker exec -it idea3-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
docker exec -it idea3-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group payment-workers
```

## 설계 포인트
- 읽기 전용 점검 명령과 변경 명령을 분리해 관리한다.
- offset reset, topic delete는 승인 절차를 강제한다.
- 점검 스크립트를 표준화해 사람 의존도를 낮춘다.

## 자주 하는 실수
1. 운영 중 수동 명령 오타로 설정 훼손
2. 변경 이력 기록 누락
3. 점검 명령을 개인 노하우로만 보관

## 요약
- CLI 운영 표준은 장애 예방 장치다.
- 변경성 명령은 통제 절차가 필수다.

## 초보자 체크
- 점검 명령과 변경 명령을 구분해 말할 수 있는가?
- 변경 명령 실행 전 필수 확인 항목 3개를 말할 수 있는가?

## 연습문제
### 기초
1. 일일 점검 명령 리스트를 작성해보세요.
2. group describe 출력에서 핵심 필드를 골라보세요.

### 응용
1. offset reset 승인 절차를 작성해보세요.
2. 운영 점검 자동화 스크립트 항목을 설계해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
