# Swagger API 문서 가이드

## 개요

YearDream 백엔드 서비스의 API 문서는 Swagger UI를 통해 제공됩니다. 이 가이드는 Swagger UI 사용법과 API 테스트 방법을 설명합니다.

## Swagger UI 접속

### 로컬 개발 환경
- URL: http://localhost:8080/swagger-ui/index.html
- API 문서 JSON: http://localhost:8080/api-docs

### 개발 서버
- URL: https://dev-api.yeardream.com/swagger-ui/index.html
- API 문서 JSON: https://dev-api.yeardream.com/api-docs

## 주요 기능

### 1. 캘린더 이벤트 관리 (Calendar Events)
- **POST /api/calendar/events** - 새 이벤트 생성
- **GET /api/calendar/events** - 사용자 이벤트 목록 조회
- **GET /api/calendar/events/{eventId}** - 특정 이벤트 조회
- **PUT /api/calendar/events/{eventId}** - 이벤트 수정
- **DELETE /api/calendar/events/{eventId}** - 이벤트 삭제
- **PATCH /api/calendar/events/{eventId}/status** - 이벤트 상태 변경

#### 날짜별 조회 API
- **GET /api/calendar/events/range** - 날짜 범위별 이벤트 조회
- **GET /api/calendar/events/month/{year}/{month}** - 월별 이벤트 조회
- **GET /api/calendar/events/week/{year}/{week}** - 주별 이벤트 조회
- **GET /api/calendar/events/daily/{date}** - 일별 이벤트 조회

#### 고급 조회 API
- **GET /api/calendar/events/category/{categoryId}** - 카테고리별 이벤트 조회
- **GET /api/calendar/events/overlapping** - 겹치는 이벤트 확인
- **GET /api/calendar/events/shared** - 공유된 이벤트 조회

### 2. 이벤트 카테고리 관리 (Event Categories)
- **POST /api/calendar/categories** - 새 카테고리 생성
- **GET /api/calendar/categories** - 사용자 카테고리 목록 조회
- **GET /api/calendar/categories/{categoryId}** - 특정 카테고리 조회
- **PUT /api/calendar/categories/{categoryId}** - 카테고리 수정
- **DELETE /api/calendar/categories/{categoryId}** - 카테고리 삭제

#### 카테고리 유틸리티 API
- **GET /api/calendar/categories/with-events** - 이벤트가 있는 카테고리 조회
- **GET /api/calendar/categories/without-events** - 이벤트가 없는 카테고리 조회
- **GET /api/calendar/categories/check-duplicate** - 카테고리 이름 중복 확인
- **GET /api/calendar/categories/count** - 사용자 카테고리 총 개수 조회

## API 인증

### JWT Bearer Token 인증
모든 API 요청에는 JWT 토큰이 필요합니다.

1. **로그인하여 토큰 획득**
   ```bash
   POST /api/auth/login
   {
     "username": "your-username",
     "password": "your-password"
   }
   ```

2. **토큰을 Authorization 헤더에 포함**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### Swagger UI에서 인증 설정
1. Swagger UI 상단의 **Authorize** 버튼 클릭
2. **bearerAuth** 섹션에 토큰 입력 (Bearer 접두사 제외)
3. **Authorize** 버튼 클릭하여 인증 완료

## 데이터 형식

### 날짜/시간 형식
- **형식**: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
- **예시**: 2024-12-25T14:30:00
- **시간대**: KST (한국 표준시)

### 색상 코드 형식
- **형식**: HEX 색상 코드
- **예시**: #FF0000 (빨간색), #00FF00 (초록색), #0000FF (파란색)

## API 테스트 예시

### 1. 이벤트 생성
```json
POST /api/calendar/events
{
  "title": "팀 회의",
  "description": "주간 팀 회의 - 프로젝트 진행 상황 공유",
  "startTime": "2024-12-25T10:00:00",
  "endTime": "2024-12-25T11:00:00",
  "location": "회의실 A",
  "categoryId": 1,
  "reminderMinutes": [30, 60]
}
```

### 2. 카테고리 생성
```json
POST /api/calendar/categories
{
  "name": "업무",
  "color": "#FF0000",
  "description": "업무 관련 일정"
}
```

### 3. 월별 이벤트 조회
```
GET /api/calendar/events/month/2024/12
```

### 4. 날짜 범위별 이벤트 조회
```
GET /api/calendar/events/range?startDate=2024-12-01&endDate=2024-12-31
```

## 에러 응답 형식

모든 에러 응답은 다음과 같은 형식을 따릅니다:

```json
{
  "timestamp": "2024-12-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "상세 에러 메시지",
  "fieldErrors": {
    "fieldName": "필드별 에러 메시지"
  }
}
```

### 주요 HTTP 상태 코드
- **200 OK**: 요청 성공
- **201 Created**: 리소스 생성 성공
- **204 No Content**: 삭제 성공
- **400 Bad Request**: 잘못된 요청 데이터
- **401 Unauthorized**: 인증 필요
- **403 Forbidden**: 접근 권한 없음
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 데이터 충돌 (중복 등)

## 개발 팁

### 1. Try it out 기능 활용
- Swagger UI의 각 API 엔드포인트에서 **Try it out** 버튼을 클릭
- 파라미터를 입력하고 **Execute** 버튼으로 실제 API 호출 테스트

### 2. 예시 데이터 활용
- 각 API의 Request Body 섹션에서 **Example Value** 클릭
- 미리 정의된 예시 데이터를 자동으로 입력

### 3. 응답 스키마 확인
- 각 API의 Responses 섹션에서 응답 데이터 구조 확인
- Model 탭에서 상세한 필드 정보 확인

### 4. 필터링 및 검색
- Swagger UI 상단의 필터 기능으로 원하는 API 빠르게 찾기
- 태그별로 API 그룹화되어 있어 카테고리별 탐색 가능

## 문제 해결

### 1. 인증 오류 (401 Unauthorized)
- JWT 토큰이 만료되었거나 유효하지 않음
- 새로 로그인하여 토큰을 갱신하세요

### 2. 권한 오류 (403 Forbidden)
- 해당 리소스에 대한 접근 권한이 없음
- 리소스 소유자이거나 적절한 권한이 있는지 확인하세요

### 3. 데이터 형식 오류 (400 Bad Request)
- 요청 데이터의 형식이 올바르지 않음
- API 문서의 스키마와 예시를 참고하여 올바른 형식으로 요청하세요

### 4. 리소스 없음 (404 Not Found)
- 요청한 리소스가 존재하지 않거나 접근 권한이 없음
- ID가 올바른지, 해당 리소스가 실제로 존재하는지 확인하세요

## 추가 정보

- **API 버전**: v1.0.0
- **문서 업데이트**: 코드 변경 시 자동 업데이트
- **지원**: dev@yeardream.com
- **라이센스**: MIT License