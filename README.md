# hhplus-be9-commerce

----
## Architecture
```
📁 server
│
├── 📁 **domain**: 도메인 계층
│   │  - 프로젝트의 가장 핵심적인 비즈니스 규칙과 데이터를 정의합니다.
│   │
│   ├── 📁 model/           # 순수 도메인 객체 (핵심 비즈니스 규칙 포함)
│   └── 📁 repository/      # 데이터 영속성을 위한 인터페이스 정의
│
├── 📁 **application**: 응용 계층
│   │  - 도메인 서비스를 조합하여 사용 사례(Use Case)를 구현합니다.
│   │  - 트랜잭션 관리 등 비즈니스 흐름을 담당합니다.
│   │
│   ├── 📁 usecase/         # 핵심 비즈니스 로직 (유스케이스)
│   └── 📁 dto/             # 유스케이스 전용 데이터 전송 객체
│
├── 📁 **interfaces**: 인터페이스 계층
│   │  - 상호작용을 담당합니다.
│   │
│   ├── 📁 api/             # 외부 요청(HTTP)을 처리하는 컨트롤러
│   └── 📁 dto/             # API 요청/응답을 위한 데이터 전송 객체
│       ├── 📁 request/
│       └── 📁 response/
│
└── 📁 **infrastructure**: 인프라 계층
    │  - 데이터베이스, 외부 API 연동 등 외부 기술을 구현합니다.
    │  - Domain 계층의 인터페이스(Port)를 직접 구현하는 어댑터(Adapter)가 위치합니다.
    │
    ├── 📁 entity/          # 데이터베이스 테이블과 1:1로 매핑되는 객체
    ├── 📁 repository/      # Domain의 Repository 인터페이스 구현체
    └── 📁 mapper/          # 도메인 모델 ↔ DB 엔티티 간 데이터 변환
```
