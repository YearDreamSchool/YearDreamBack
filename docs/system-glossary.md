# 시스템 용어사전

## 개요
이 문서는 Yeardream Back 시스템에서 사용되는 주요 용어들을 정의하고 설명합니다. 개발자, 운영자, 사용자가 시스템을 이해하고 소통할 때 참고할 수 있는 표준 용어집입니다.

---

## 도메인 용어

### 사용자 관리 (User Management)

#### User (사용자)
- **정의**: 시스템에 등록된 개별 사용자
- **속성**: 
  - `username`: 사용자 고유 식별자 (로그인 ID)
  - `name`: 사용자 실명 또는 별명
  - `email`: 이메일 주소
  - `role`: 사용자 권한 등급
  - `profileImg`: 프로필 이미지 URL
  - `phone`: 전화번호
- **관련 용어**: UserRoleType, OAuth2

#### UserRoleType (사용자 권한 유형)
- **ADMIN**: 시스템 관리자 권한을 가진 운영진
- **COACH**: 코치 권한을 가진 지도자
- **USER**: 일반 사용자 권한을 가진 교육생

### 캘린더 시스템 (Calendar System)

#### CalendarEvent (캘린더 이벤트)
- **정의**: 사용자의 일정 정보를 저장하는 핵심 엔티티
- **속성**:
  - `title`: 이벤트 제목 (필수, 최대 100자)
  - `description`: 이벤트 설명 (최대 500자)
  - `startTime`: 이벤트 시작 시간 (필수)
  - `endTime`: 이벤트 종료 시간 (필수)
  - `location`: 이벤트 장소 (최대 200자)
  - `status`: 이벤트 상태
- **제약사항**: 
  - 시작 시간은 종료 시간보다 이전이어야 함
  - 최대 이벤트 기간은 7일
- **관련 용어**: EventStatus, EventCategory, EventReminder, EventShare

#### EventStatus (이벤트 상태)
- **SCHEDULED**: 예정됨 - 아직 시작되지 않은 이벤트
- **IN_PROGRESS**: 진행중 - 현재 진행 중인 이벤트
- **COMPLETED**: 완료됨 - 종료된 이벤트
- **CANCELLED**: 취소됨 - 취소된 이벤트

#### EventCategory (이벤트 카테고리)
- **정의**: 사용자가 이벤트를 분류하기 위해 생성하는 카테고리
- **속성**:
  - `name`: 카테고리 이름 (필수, 최대 50자)
  - `color`: 카테고리 색상 (HEX 코드 형식, 예: #FF0000)
  - `description`: 카테고리 설명 (최대 200자)
- **제약사항**: 
  - 이벤트가 있는 카테고리는 삭제 불가
  - 색상이 없으면 기본 파란색(#3498db) 적용

#### EventReminder (이벤트 알림)
- **정의**: 이벤트에 대한 알림 설정 정보
- **속성**:
  - `minutesBefore`: 이벤트 시작 전 몇 분 전에 알림 (0분 이상)
  - `isActive`: 알림 활성화 여부
- **제약사항**: 최대 1주일(10,080분) 전까지만 알림 설정 가능

#### EventShare (이벤트 공유)
- **정의**: 이벤트를 다른 사용자와 공유하는 정보
- **속성**:
  - `sharedWithUser`: 공유받은 사용자
  - `permission`: 공유 권한
- **제약사항**: 자기 자신과는 공유 불가

#### SharePermission (공유 권한)
- **VIEW_ONLY**: 읽기 전용 - 이벤트 조회만 가능
- **EDIT**: 편집 가능 - 이벤트 수정 가능

### 학생 관리 (Student Management)

#### Students (학생)
- **정의**: 교육 과정에 참여하는 학생 정보
- **속성**:
  - `name`: 학생 이름
  - `seatNumber`: 좌석 번호
  - `status`: 출결 상태
- **관련 용어**: StudentStatus

#### StudentStatus (학생 출결 상태)
- **PRESENT**: 출석 - 정상 출석한 상태
- **ABSENT**: 결석 - 출석하지 않은 상태
- **LATE**: 지각 - 지각하거나 QR코드 스캔은 했으나 오프라인 출석하지 않은 상태

---

## 기술 용어

### 인증 및 보안 (Authentication & Security)

#### JWT (JSON Web Token)
- **정의**: 사용자 인증을 위한 토큰 기반 인증 방식
- **구성요소**: Header, Payload, Signature
- **관련 클래스**: JWTUtil, JWTFilter

#### OAuth2
- **정의**: 제3자 인증 서비스를 통한 로그인 방식
- **지원 제공자**: Google, Naver, Kakao
- **관련 클래스**: CustomOAuth2UserService, CustomSuccessHandler

#### RefreshToken (리프레시 토큰)
- **정의**: JWT 토큰 갱신을 위한 장기 보관 토큰
- **보안**: 데이터베이스에 암호화되어 저장

### API 및 웹 서비스

#### REST API
- **정의**: RESTful 아키텍처를 따르는 웹 API
- **HTTP 메서드**: GET, POST, PUT, DELETE
- **응답 형식**: JSON

#### Swagger/OpenAPI
- **정의**: API 문서화 및 테스트 도구
- **접속 경로**: `/swagger-ui.html`
- **API 문서**: `/v3/api-docs`

#### CORS (Cross-Origin Resource Sharing)
- **정의**: 다른 도메인에서의 리소스 접근을 허용하는 보안 정책
- **허용 도메인**: `http://localhost:3000` (프론트엔드)

### 데이터베이스

#### JPA (Java Persistence API)
- **정의**: 자바 객체와 관계형 데이터베이스 간의 매핑을 위한 표준
- **구현체**: Hibernate

#### Entity (엔티티)
- **정의**: 데이터베이스 테이블과 매핑되는 자바 객체
- **어노테이션**: `@Entity`, `@Table`, `@Id`

#### Repository (리포지토리)
- **정의**: 데이터 접근 계층의 인터페이스
- **상속**: JpaRepository 인터페이스 확장

---

## 비즈니스 용어

### 일정 관리

#### 이벤트 생성 (Event Creation)
- **정의**: 새로운 캘린더 이벤트를 시스템에 등록하는 과정
- **필수 정보**: 제목, 시작시간, 종료시간, 소유자

#### 이벤트 공유 (Event Sharing)
- **정의**: 개인 이벤트를 다른 사용자와 공유하는 기능
- **권한 유형**: 읽기 전용, 편집 가능

#### 알림 설정 (Reminder Setting)
- **정의**: 이벤트 시작 전 사용자에게 알림을 보내는 기능
- **알림 시점**: 이벤트 시작 N분 전

#### 카테고리 분류 (Category Classification)
- **정의**: 이벤트를 유형별로 분류하여 관리하는 기능
- **시각적 구분**: 색상 코드를 통한 구분

### 접근 제어

#### 소유권 (Ownership)
- **정의**: 이벤트나 카테고리에 대한 생성자의 권한
- **권한**: 수정, 삭제, 공유 설정

#### 접근 권한 (Access Permission)
- **정의**: 공유된 리소스에 대한 사용자의 권한 수준
- **검증**: AccessControlService를 통한 권한 확인

---

## 시스템 아키텍처 용어

### 계층 구조 (Layered Architecture)

#### Controller Layer (컨트롤러 계층)
- **역할**: HTTP 요청 처리 및 응답 반환
- **어노테이션**: `@RestController`, `@RequestMapping`

#### Service Layer (서비스 계층)
- **역할**: 비즈니스 로직 처리
- **어노테이션**: `@Service`, `@Transactional`

#### Repository Layer (리포지토리 계층)
- **역할**: 데이터 접근 및 영속성 관리
- **어노테이션**: `@Repository`

#### DTO (Data Transfer Object)
- **정의**: 계층 간 데이터 전송을 위한 객체
- **종류**: Request DTO, Response DTO

### 예외 처리

#### Custom Exception (커스텀 예외)
- **EventNotFoundException**: 이벤트를 찾을 수 없을 때
- **InvalidEventTimeException**: 이벤트 시간이 유효하지 않을 때
- **EventSharingException**: 이벤트 공유 중 오류 발생 시
- **AccessDeniedException**: 접근 권한이 없을 때

#### Global Exception Handler
- **정의**: 전역 예외 처리를 담당하는 컴포넌트
- **어노테이션**: `@ControllerAdvice`, `@ExceptionHandler`

---

## 개발 및 운영 용어

### 개발 환경

#### Spring Boot
- **정의**: 스프링 기반 애플리케이션 개발 프레임워크
- **버전**: 3.5.5

#### Gradle
- **정의**: 빌드 자동화 도구
- **설정 파일**: `build.gradle`

#### H2 Database
- **정의**: 개발 및 테스트용 인메모리 데이터베이스
- **콘솔**: `/h2-console`

#### MySQL
- **정의**: 운영 환경에서 사용하는 관계형 데이터베이스

### 테스트

#### Unit Test (단위 테스트)
- **정의**: 개별 컴포넌트의 기능을 검증하는 테스트
- **프레임워크**: JUnit 5

#### Integration Test (통합 테스트)
- **정의**: 여러 컴포넌트 간의 상호작용을 검증하는 테스트
- **어노테이션**: `@SpringBootTest`

### 모니터링 및 로깅

#### Logging
- **프레임워크**: Log4j2
- **레벨**: ERROR, WARN, INFO, DEBUG

#### Actuator
- **정의**: 애플리케이션 모니터링 및 관리 기능
- **엔드포인트**: `/actuator`

---

## 약어 및 축약어

- **API**: Application Programming Interface
- **DTO**: Data Transfer Object
- **JPA**: Java Persistence API
- **JWT**: JSON Web Token
- **CORS**: Cross-Origin Resource Sharing
- **CRUD**: Create, Read, Update, Delete
- **HTTP**: HyperText Transfer Protocol
- **JSON**: JavaScript Object Notation
- **REST**: Representational State Transfer
- **SQL**: Structured Query Language
- **URL**: Uniform Resource Locator
- **UUID**: Universally Unique Identifier

---

## 참고사항

### 명명 규칙
- **클래스명**: PascalCase (예: CalendarEvent)
- **메서드명**: camelCase (예: createEvent)
- **상수명**: UPPER_SNAKE_CASE (예: MAX_EVENT_DURATION)
- **데이터베이스 테이블명**: snake_case (예: calendar_events)

### 코딩 컨벤션
- **패키지 구조**: 도메인별 분리 (calendar, user, student)
- **어노테이션 순서**: 스프링 어노테이션 → 검증 어노테이션 → 기타
- **주석**: 한국어로 작성, JavaDoc 형식 준수

---

*이 용어사전은 시스템 발전에 따라 지속적으로 업데이트됩니다.*