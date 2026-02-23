# Chapter 27 - Spring Boot Kafka 기초

- 상태: 초안 완료
- 목표 분량: 8쪽

## 학습 목표
- Spring Kafka 기본 구성 요소를 설명할 수 있다.
- producer/consumer 코드 구조를 설계할 수 있다.
- 통합 테스트 기본 전략을 수립할 수 있다.

## 핵심 개념

Spring Kafka 통합의 핵심은
"직렬화 규칙 + 에러 처리 + 재시도 전략" 표준화입니다.
연결만 성공해도 운영은 실패할 수 있습니다.

## 실습 예제

실습 목적: Spring Kafka 소비자 테스트가 로컬에서 안정적으로 실행되는지 확인합니다.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: payment-workers
```

예상 결과/관찰 포인트:
- KafkaTemplate/리스너 의존 소비자 테스트가 통과해야 합니다.
- 핸들러 예외 경로에서도 테스트가 실패하지 않도록 모킹이 일관돼야 합니다.

## 예제 명령어 검증 시나리오(권장)

사전 조건:
- Gradle 실행 가능

검증 절차:
1. `./gradlew :worker:test --tests "*NotificationRequestQueuedConsumerTest"` 실행
2. 테스트 통과 여부 확인

성공 기준:
- 대상 테스트가 `BUILD SUCCESSFUL`로 종료됨
- consumer 처리/에러 경로 테스트가 모두 통과함

## 설계 포인트
- 토픽별 producer/consumer 설정을 분리한다.
- 공통 에러 핸들러와 DLQ 전략을 표준화한다.
- integration test에서 재시도/중복 소비를 검증한다.

## 자주 하는 실수
1. 모든 리스너에 동일 에러 전략 사용
2. 직렬화 포맷 변경 절차 없음
3. 통합 테스트에서 lag/재시도 케이스 누락

## 요약
- Spring Kafka는 연결보다 운영 규칙이 중요하다.
- 에러 처리와 직렬화 표준을 먼저 고정해야 한다.

## 연습문제
### 기초
1. 기본 producer/consumer 설정 파일을 작성해보세요.
2. 공통 에러 핸들러 설계 포인트를 적어보세요.

### 응용
1. 토픽별 리스너 분리 전략을 설계해보세요.
2. 통합 테스트 체크리스트를 작성해보세요.

## 챕터 체크리스트
- [x] 초안 작성 완료
- [x] 예제 명령어 검증 완료
- [x] 초보자 기준 용어 설명 완료
- [x] 최종 교정 완료
