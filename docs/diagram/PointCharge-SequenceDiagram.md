## 잔액 충전
```mermaid
sequenceDiagram
    actor User as 사용자
    participant Client as 클라이언트
    participant Controller as PointController
    participant Service as PointService
    participant Repository as PointRepository

    User->>+Client: 잔액 충전 요청
    Client->>+Controller: 값 전달
    alt 정상 입력 값
        Controller->>+Service: 잔액 충전 처리
        Service->>Repository: 사용자 잔액 조회
        Repository-->>Service: 현재 잔액 반환
        Service->>Service: 잔액 계산
        Service->>Repository: 잔액 업데이트
        Repository-->>Service: 업데이트 성공
        Service->>Repository: 충전 이력 저장
        Repository-->>Service: 충전 이력 저장 완료
        Service-->>-Controller: 처리 성공
        Controller-->>Client: 잔액 충전 성공 
    else 잘못된 입력 값
        Controller-->>-Client: 잔액 충전 실패
    end
    Client-->>-User: 충전 완료 표시
```