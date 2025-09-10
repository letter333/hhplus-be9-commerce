# 쿠폰 발급 시스템 Kafka 설계 문서
### 1. 설계 목적
- **대용량 쿠폰 발급 요청**에 대한 안정적이고 확장 가능한 처리
- **비동기 메시징**을 통한 시스템 성능 및 확장성 향상
- **데이터 영속성 보장**을 통한 안정성 확보

### 2. 기존 Redis 기반 시스템의 한계점
#### 2.1 데이터 손실 위험
- Redis의 메모리 기반 특성으로 인한 서버 재시작 시 데이터 손실 위험
- RDB/AOF 설정이 없거나 대용량 데이터 복구 시 긴 로딩 시간 소요

#### 2.2 백업 및 복구의 복잡성
- 메모리 덤프 생성 시 실시간 성능에 미치는 영향
- 특정 시점으로의 정확한 데이터 복구 어려움

#### 2.3 확장성 제약
- 단일 Redis 인스턴스의 메모리 용량 한계
- 대용량 메모리 사용 시 GC로 인한 지연 발생
- 수직적 확장에만 의존하는 구조적 한계

### 3. Kafka 도입을 통한 개선 효과
#### 3.1 영속성 보장
``` 
기존: Redis (임시 저장) → 스케줄러 → RDB (영구 저장)
개선: Redis (선착순 보장) → Kafka (메시지 큐) → Consumer → RDB (영구 저장)
```
#### 3.2 확장성 향상
- **수평적 확장**: 파티션 기반 분산 처리
- **병렬 처리**: 컨슈머 그룹을 통한 독립적 처리
- **유연한 처리량 조절**: 파티션 수 = 최대 동시 처리 인스턴스 수

#### 3.3 안정성 강화
- **메시지 영속화**: 디스크 기반 저장으로 데이터 손실 방지
- **재처리 보장**: 오프셋 관리를 통한 정확한 메시지 처리
- **장애 격리**: Producer와 Consumer 간 비동기 처리로 장애 전파 차단

### 4. 아키텍처 변경 사항
#### 4.1 처리 흐름 분리
- **기존**: Redis에서 선착순 보장 + 쿠폰 발급을 동기적으로 처리
- **변경**: Redis(선착순 보장) + Kafka(비동기 발급 처리)로 역할 분리

#### 4.2 Kafka 토픽 설정
``` java
// 현재 설정: 3개 파티션, 1개 복제본
TopicBuilder.name("coupon-issue")
    .partitions(3)
    .replicas(1)
    .build()
```
### 5. 시스템 플로우
``` mermaid
sequenceDiagram
    participant Client as 클라이언트
    participant API as API Controller
    participant UseCase as CouponIssueUseCase
    participant Redis as Redis
    participant KafkaP as Kafka Producer
    participant KafkaT as Kafka Topic
    participant KafkaC as Kafka Consumer
    participant DB as Database

    Client->>API: 쿠폰 발급 요청
    API->>UseCase: CouponIssueCommand 전달

    UseCase->>Redis: 발급 가능 여부 확인
    alt 발급 가능
        Redis->>Redis: 발급 수량 증가
        UseCase->>KafkaP: CouponIssueEvent 발행
        KafkaP->>KafkaT: 이벤트 전송 (비동기)
        
        UseCase-->>API: UserCoupon 응답
        API-->>Client: 발급 성공 응답

        Note over KafkaT,KafkaC: 백그라운드 처리
        KafkaT->>KafkaC: 이벤트 수신
        KafkaC->>DB: 실제 쿠폰 데이터 저장
        
        alt DB 저장 성공
            KafkaC->>KafkaC: 오프셋 커밋
            Note over KafkaC: 성공 로그 기록
        else DB 저장 실패
            KafkaC->>KafkaC: 재시도 또는 DLQ 처리
            Note over KafkaC: 실패 알림 및 로그
        end

    else 발급 불가능 (수량 초과/중복 발급)
        UseCase-->>API: Exception
        API-->>Client: 400 Bad Request
    end
```
### 6. 핵심 컴포넌트
#### 6.1 이벤트 구조
``` java
@Builder
public record CouponIssueEvent(
    Long couponId,
    Long userId,
    String couponCode,
    LocalDateTime expiredAt
) {}
```
#### 6.2 처리 흐름
1. **선착순 검증**: Redis를 통한 실시간 수량 관리
2. **이벤트 발행**: Kafka Producer를 통한 비동기 메시지 전송
3. **백그라운드 처리**: Consumer를 통한 DB 저장

### 7. 향후 개선 방향
#### 7.1 Outbox 패턴 적용
- 트랜잭션 안정성 보장을 위한 Outbox 테이블 도입
- DB 트랜잭션과 Kafka 메시지 발행의 원자성 보장

#### 7.2 DLQ(Dead Letter Queue) 구현