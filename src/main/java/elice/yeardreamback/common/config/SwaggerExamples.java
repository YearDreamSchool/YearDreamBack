package elice.yeardreamback.common.config;

/**
 * Swagger API 문서화를 위한 예시 데이터 상수 클래스
 * 일관된 예시 데이터를 제공하여 API 문서의 품질을 향상시킵니다.
 */
public final class SwaggerExamples {

    private SwaggerExamples() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * 캘린더 이벤트 관련 예시 데이터
     */
    public static final class CalendarEvent {
        
        public static final String CREATE_REQUEST = """
            {
                "title": "팀 회의",
                "description": "주간 팀 회의 - 프로젝트 진행 상황 공유 및 다음 주 계획 수립",
                "startTime": "2024-12-25T10:00:00",
                "endTime": "2024-12-25T11:00:00",
                "location": "회의실 A (3층)",
                "categoryId": 1,
                "reminderMinutes": [30, 60]
            }
            """;

        public static final String UPDATE_REQUEST = """
            {
                "title": "팀 회의 (수정됨)",
                "description": "주간 팀 회의 - 프로젝트 진행 상황 공유 및 다음 주 계획 수립 (장소 변경)",
                "startTime": "2024-12-25T14:00:00",
                "endTime": "2024-12-25T15:00:00",
                "location": "회의실 B (2층)",
                "categoryId": 1,
                "reminderMinutes": [15, 30]
            }
            """;

        public static final String RESPONSE = """
            {
                "id": 1,
                "title": "팀 회의",
                "description": "주간 팀 회의 - 프로젝트 진행 상황 공유",
                "startTime": "2024-12-25T10:00:00",
                "endTime": "2024-12-25T11:00:00",
                "location": "회의실 A (3층)",
                "status": "SCHEDULED",
                "category": {
                    "id": 1,
                    "name": "업무",
                    "color": "#FF0000",
                    "description": "업무 관련 일정"
                },
                "reminders": [
                    {
                        "id": 1,
                        "minutesBefore": 30,
                        "isActive": true
                    },
                    {
                        "id": 2,
                        "minutesBefore": 60,
                        "isActive": true
                    }
                ],
                "ownerUsername": "user123",
                "isShared": false,
                "canEdit": true,
                "createdAt": "2024-12-20T09:00:00",
                "updatedAt": "2024-12-20T09:30:00"
            }
            """;

        public static final String LIST_RESPONSE = """
            [
                {
                    "id": 1,
                    "title": "팀 회의",
                    "description": "주간 팀 회의",
                    "startTime": "2024-12-25T10:00:00",
                    "endTime": "2024-12-25T11:00:00",
                    "location": "회의실 A",
                    "status": "SCHEDULED",
                    "category": {
                        "id": 1,
                        "name": "업무",
                        "color": "#FF0000"
                    },
                    "reminders": [
                        {
                            "id": 1,
                            "minutesBefore": 30,
                            "isActive": true
                        }
                    ],
                    "ownerUsername": "user123",
                    "isShared": false,
                    "canEdit": true
                },
                {
                    "id": 2,
                    "title": "개인 약속",
                    "description": "친구와 점심 식사",
                    "startTime": "2024-12-25T12:00:00",
                    "endTime": "2024-12-25T13:00:00",
                    "location": "레스토랑 XYZ",
                    "status": "SCHEDULED",
                    "category": {
                        "id": 2,
                        "name": "개인",
                        "color": "#00FF00"
                    },
                    "reminders": [],
                    "ownerUsername": "user123",
                    "isShared": false,
                    "canEdit": true
                }
            ]
            """;
    }

    /**
     * 이벤트 카테고리 관련 예시 데이터
     */
    public static final class EventCategory {
        
        public static final String CREATE_REQUEST = """
            {
                "name": "업무",
                "color": "#FF0000",
                "description": "업무 관련 일정 및 회의"
            }
            """;

        public static final String UPDATE_REQUEST = """
            {
                "name": "업무 (수정됨)",
                "color": "#FF5500",
                "description": "업무 관련 일정, 회의 및 프로젝트 관리"
            }
            """;

        public static final String RESPONSE = """
            {
                "id": 1,
                "name": "업무",
                "color": "#FF0000",
                "description": "업무 관련 일정 및 회의",
                "eventCount": 5,
                "createdAt": "2024-12-01T09:00:00",
                "updatedAt": "2024-12-01T09:00:00"
            }
            """;

        public static final String LIST_RESPONSE = """
            [
                {
                    "id": 1,
                    "name": "업무",
                    "color": "#FF0000",
                    "description": "업무 관련 일정",
                    "eventCount": 5
                },
                {
                    "id": 2,
                    "name": "개인",
                    "color": "#00FF00",
                    "description": "개인적인 약속",
                    "eventCount": 3
                },
                {
                    "id": 3,
                    "name": "운동",
                    "color": "#0000FF",
                    "description": "운동 및 건강 관리",
                    "eventCount": 0
                }
            ]
            """;
    }

    /**
     * 이벤트 공유 관련 예시 데이터
     */
    public static final class EventShare {
        
        public static final String CREATE_REQUEST = """
            {
                "sharedWithUsername": "user456",
                "permission": "VIEW_ONLY"
            }
            """;

        public static final String UPDATE_REQUEST = """
            {
                "sharedWithUsername": "user456",
                "permission": "EDIT"
            }
            """;

        public static final String RESPONSE = """
            {
                "id": 1,
                "eventId": 123,
                "sharedByUsername": "user123",
                "sharedWithUsername": "user456",
                "permission": "VIEW_ONLY",
                "sharedAt": "2024-12-20T10:00:00"
            }
            """;
    }

    /**
     * 공통 에러 응답 예시
     */
    public static final class ErrorResponse {
        
        public static final String VALIDATION_ERROR = """
            {
                "timestamp": "2024-12-20T10:00:00",
                "status": 400,
                "error": "Validation Failed",
                "message": "입력 데이터 유효성 검사에 실패했습니다",
                "fieldErrors": {
                    "title": "이벤트 제목은 필수입니다",
                    "startTime": "이벤트 시작 시간은 필수입니다"
                }
            }
            """;

        public static final String NOT_FOUND_ERROR = """
            {
                "timestamp": "2024-12-20T10:00:00",
                "status": 404,
                "error": "Not Found",
                "message": "요청한 리소스를 찾을 수 없습니다"
            }
            """;

        public static final String UNAUTHORIZED_ERROR = """
            {
                "timestamp": "2024-12-20T10:00:00",
                "status": 401,
                "error": "Unauthorized",
                "message": "인증이 필요합니다"
            }
            """;

        public static final String FORBIDDEN_ERROR = """
            {
                "timestamp": "2024-12-20T10:00:00",
                "status": 403,
                "error": "Forbidden",
                "message": "해당 리소스에 대한 접근 권한이 없습니다"
            }
            """;

        public static final String CONFLICT_ERROR = """
            {
                "timestamp": "2024-12-20T10:00:00",
                "status": 409,
                "error": "Conflict",
                "message": "이미 존재하는 데이터입니다"
            }
            """;
    }

    /**
     * API 사용 가이드 및 팁
     */
    public static final class ApiGuide {
        
        public static final String DATE_TIME_FORMAT = """
            날짜/시간 형식 가이드:
            - ISO 8601 형식을 사용합니다: YYYY-MM-DDTHH:mm:ss
            - 예시: 2024-12-25T14:30:00
            - 시간대는 서버 시간대(KST)를 기준으로 합니다
            """;

        public static final String PAGINATION_INFO = """
            페이지네이션 정보:
            - page: 페이지 번호 (0부터 시작)
            - size: 페이지 크기 (기본값: 20, 최대값: 100)
            - sort: 정렬 기준 (예: startTime,asc 또는 title,desc)
            """;

        public static final String AUTHENTICATION_INFO = """
            인증 정보:
            - JWT Bearer 토큰을 Authorization 헤더에 포함해야 합니다
            - 형식: Authorization: Bearer {your-jwt-token}
            - 토큰 만료 시 401 Unauthorized 응답을 받게 됩니다
            """;
    }
}