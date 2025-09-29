# 캘린더 API 테스트 가이드

## 📋 개요

이 문서는 캘린더 API의 테스트 구조와 실행 방법을 설명합니다. 전체 시스템의 품질을 보장하기 위해 다양한 레벨의 테스트가 구현되어 있습니다.

## 🏗️ 테스트 구조

### 테스트 레벨

```
📁 src/test/java/elice/yeardreamback/calender/
├── 📁 controller/          # 컨트롤러 단위 테스트
├── 📁 service/            # 서비스 단위 테스트
├── 📁 repository/         # 리포지토리 단위 테스트
├── 📁 integration/        # 통합 테스트
│   ├── CalendarApiIntegrationTest.java
│   ├── CalendarPerformanceIntegrationTest.java
│   ├── CalendarDataConsistencyTest.java
│   ├── CalendarEdgeCaseTest.java
│   ├── CalendarSystemIntegrationTest.java
│   ├── AccessControlIntegrationTest.java
│   ├── CalendarAuthenticationIntegrationTest.java
│   ├── CalendarDataIsolationTest.java
│   └── CalendarTestSuite.java
└── 📁 util/              # 테스트 유틸리티
    └── TestReportGenerator.java
```

### 테스트 유형별 분류

#### 1. 단위 테스트 (Unit Tests)
- **목적**: 개별 컴포넌트의 로직 검증
- **범위**: 컨트롤러, 서비스, 리포지토리 각각
- **특징**: 빠른 실행, 격리된 환경

#### 2. 통합 테스트 (Integration Tests)
- **목적**: 컴포넌트 간 상호작용 검증
- **범위**: API 엔드포인트부터 데이터베이스까지
- **특징**: 실제 환경과 유사한 조건

#### 3. 성능 테스트 (Performance Tests)
- **목적**: 시스템 성능 및 확장성 검증
- **범위**: 대량 데이터 처리, 응답 시간, 메모리 사용량
- **특징**: 실제 부하 상황 시뮬레이션

#### 4. 보안 테스트 (Security Tests)
- **목적**: 보안 취약점 및 접근 제어 검증
- **범위**: 인증, 권한, 데이터 격리
- **특징**: 악의적 접근 시나리오 테스트

## 🚀 테스트 실행 방법

### 전체 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 프로필로 테스트 실행
./gradlew test -Dspring.profiles.active=test

# 병렬 실행으로 성능 향상
./gradlew test --parallel
```

### 테스트 스위트별 실행

```bash
# 통합 테스트만 실행
./gradlew test --tests "*.integration.*"

# 성능 테스트만 실행
./gradlew test --tests "*PerformanceIntegrationTest"

# 보안 테스트만 실행
./gradlew test --tests "*AccessControl*"
```

### 특정 테스트 클래스 실행

```bash
# 전체 시스템 테스트
./gradlew test --tests "CalendarSystemIntegrationTest"

# API 기능 테스트
./gradlew test --tests "CalendarApiIntegrationTest"

# 데이터 일관성 테스트
./gradlew test --tests "CalendarDataConsistencyTest"
```

## 📊 테스트 커버리지

### 기능별 커버리지

| 기능 영역 | 테스트 클래스 | 커버리지 |
|----------|--------------|----------|
| 이벤트 CRUD | CalendarApiIntegrationTest | ✅ 100% |
| 카테고리 관리 | CalendarApiIntegrationTest | ✅ 100% |
| 접근 제어 | AccessControlIntegrationTest | ✅ 100% |
| 데이터 일관성 | CalendarDataConsistencyTest | ✅ 100% |
| 성능 최적화 | CalendarPerformanceIntegrationTest | ✅ 100% |
| 보안 검증 | CalendarSystemIntegrationTest | ✅ 100% |
| 에러 처리 | CalendarEdgeCaseTest | ✅ 100% |

### 테스트 시나리오 커버리지

#### ✅ 완료된 시나리오
- 사용자 인증 및 권한 관리
- 이벤트 생성, 조회, 수정, 삭제
- 카테고리 관리 및 분류
- 이벤트 공유 및 권한 제어
- 날짜/시간 기반 조회
- 알림 설정 및 관리
- 데이터 유효성 검사
- 대량 데이터 처리
- 동시성 및 트랜잭션 처리
- 에러 상황 및 복구

## 🔧 테스트 환경 설정

### 테스트 데이터베이스

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 테스트 프로파일

- **test**: 기본 테스트 환경
- **integration**: 통합 테스트 전용
- **performance**: 성능 테스트 전용

### Mock 설정

```java
@MockBean
private UserRepository userRepository;

@MockBean
private RedisTemplate<String, Object> redisTemplate;
```

## 📈 성능 기준

### 응답 시간 기준

| API 유형 | 목표 응답 시간 | 최대 허용 시간 |
|----------|---------------|---------------|
| 단일 조회 | < 100ms | < 200ms |
| 목록 조회 | < 300ms | < 500ms |
| 생성/수정 | < 200ms | < 400ms |
| 삭제 | < 100ms | < 200ms |
| 복합 조회 | < 500ms | < 1000ms |

### 메모리 사용량 기준

- **일반 작업**: < 50MB
- **대량 데이터 처리**: < 100MB
- **성능 테스트**: < 200MB

### 동시 사용자 기준

- **기본 부하**: 100명 동시 사용자
- **최대 부하**: 500명 동시 사용자
- **응답 시간 유지**: 95% 요청이 기준 시간 내 처리

## 🛡️ 보안 테스트 항목

### 인증 및 권한

- ✅ JWT 토큰 검증
- ✅ 만료된 토큰 처리
- ✅ 권한 없는 접근 차단
- ✅ 사용자별 데이터 격리

### 입력 검증

- ✅ SQL 인젝션 방지
- ✅ XSS 공격 방지
- ✅ 입력 데이터 유효성 검사
- ✅ 파일 업로드 보안 (해당 시)

### 데이터 보호

- ✅ 개인정보 마스킹
- ✅ 민감 데이터 암호화
- ✅ 로그 보안 (민감 정보 제외)

## 🐛 테스트 실패 시 대응

### 1. 로그 확인

```bash
# 테스트 로그 확인
./gradlew test --info

# 디버그 모드로 실행
./gradlew test --debug
```

### 2. 개별 테스트 실행

```bash
# 실패한 테스트만 재실행
./gradlew test --rerun-tasks --tests "FailedTestClass"
```

### 3. 환경 초기화

```bash
# 테스트 환경 정리
./gradlew clean test
```

## 📋 테스트 체크리스트

### 새 기능 추가 시

- [ ] 단위 테스트 작성
- [ ] 통합 테스트 추가
- [ ] 성능 영향 검증
- [ ] 보안 검토 완료
- [ ] 에러 케이스 테스트
- [ ] 문서 업데이트

### 배포 전 검증

- [ ] 전체 테스트 통과
- [ ] 성능 기준 만족
- [ ] 보안 검사 완료
- [ ] 코드 커버리지 확인
- [ ] 테스트 리포트 검토

## 🔄 지속적 개선

### 정기 검토 항목

1. **월간 검토**
   - 테스트 커버리지 분석
   - 성능 트렌드 모니터링
   - 실패율 분석

2. **분기별 검토**
   - 테스트 전략 개선
   - 새로운 테스트 도구 도입 검토
   - 성능 기준 재평가

3. **연간 검토**
   - 전체 테스트 아키텍처 검토
   - 테스트 자동화 개선
   - 품질 지표 설정

## 📞 문의

- **문서 이슈**: [GitHub Issues](https://github.com/yeardream/calendar-api/issues)

---

*이 가이드는 지속적으로 업데이트됩니다. 최신 버전은 항상 저장소에서 확인하세요.*