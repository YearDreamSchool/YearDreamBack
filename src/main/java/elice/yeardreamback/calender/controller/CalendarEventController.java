package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.service.CalendarEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 이벤트 REST API 컨트롤러
 * 이벤트 CRUD 및 조회 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
@Tag(name = "Calendar Events", description = "캘린더 이벤트 관리 API")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @Operation(
        summary = "새 이벤트 생성", 
        description = "새로운 캘린더 이벤트를 생성합니다. 제목, 시작/종료 시간은 필수이며, 카테고리와 알림은 선택사항입니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "생성할 이벤트 정보",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CalendarEventRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "이벤트 생성 예시",
                    value = """
                    {
                        "title": "팀 회의",
                        "description": "주간 팀 회의",
                        "startTime": "2024-12-01T14:00:00",
                        "endTime": "2024-12-01T15:00:00",
                        "location": "회의실 A",
                        "categoryId": 1,
                        "reminderMinutes": [30, 60]
                    }
                    """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "이벤트 생성 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CalendarEventResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터 (필수 필드 누락, 잘못된 시간 범위 등)",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                        "timestamp": "2024-12-01T10:00:00",
                        "status": 400,
                        "error": "Validation Failed",
                        "message": "입력 데이터 유효성 검사에 실패했습니다",
                        "fieldErrors": {
                            "title": "이벤트 제목은 필수입니다",
                            "startTime": "이벤트 시작 시간은 필수입니다"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요 - JWT 토큰이 없거나 유효하지 않음")
    })
    @PostMapping
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CalendarEventRequest request,
            Authentication authentication) {
        
        log.info("이벤트 생성 요청: 사용자={}, 제목={}", authentication.getName(), request.getTitle());
        
        CalendarEventResponse response = calendarEventService.createEvent(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "이벤트 수정", 
        description = "기존 이벤트를 수정합니다. 이벤트 소유자만 수정할 수 있으며, 공유된 이벤트의 경우 편집 권한이 있는 사용자만 수정 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "이벤트 수정 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CalendarEventResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터 (시간 범위 오류, 유효성 검사 실패 등)"
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(
            responseCode = "404", 
            description = "이벤트를 찾을 수 없거나 수정 권한이 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                        "timestamp": "2024-12-01T10:00:00",
                        "status": 404,
                        "error": "Event Not Found",
                        "message": "이벤트를 찾을 수 없거나 접근 권한이 없습니다: 123"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{eventId}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Valid @RequestBody CalendarEventRequest request,
            Authentication authentication) {
        
        log.info("이벤트 수정 요청: 사용자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        CalendarEventResponse response = calendarEventService.updateEvent(authentication.getName(), eventId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 삭제", description = "이벤트를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            Authentication authentication) {
        
        log.info("이벤트 삭제 요청: 사용자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        calendarEventService.deleteEvent(authentication.getName(), eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이벤트 조회", description = "특정 이벤트의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이벤트 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<CalendarEventResponse> getEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            Authentication authentication) {
        
        log.debug("이벤트 조회 요청: 사용자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        CalendarEventResponse response = calendarEventService.getEvent(authentication.getName(), eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "사용자 이벤트 목록 조회", 
        description = "현재 인증된 사용자의 모든 이벤트를 시작 시간 순으로 조회합니다. 공유받은 이벤트는 포함되지 않습니다."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "이벤트 목록 조회 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(
                type = "array",
                implementation = CalendarEventResponse.class
            ),
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "이벤트 목록 예시",
                value = """
                [
                    {
                        "id": 1,
                        "title": "팀 회의",
                        "description": "주간 팀 회의",
                        "startTime": "2024-12-01T14:00:00",
                        "endTime": "2024-12-01T15:00:00",
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
                        "ownerUsername": "testuser",
                        "isShared": false,
                        "canEdit": true
                    }
                ]
                """
            )
        )
    )
    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getUserEvents(Authentication authentication) {
        log.debug("사용자 이벤트 목록 조회: 사용자={}", authentication.getName());
        
        List<CalendarEventResponse> responses = calendarEventService.getUserEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "날짜 범위별 이벤트 조회", description = "지정된 날짜 범위의 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "이벤트 목록 조회 성공")
    @GetMapping("/range")
    public ResponseEntity<List<CalendarEventResponse>> getEventsByDateRange(
            @Parameter(description = "시작 날짜 (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        log.debug("날짜 범위별 이벤트 조회: 사용자={}, 시작={}, 종료={}", 
                authentication.getName(), startDate, endDate);
        
        List<CalendarEventResponse> responses = calendarEventService.getUserEvents(
                authentication.getName(), startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "월별 이벤트 조회", description = "지정된 월의 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "월별 이벤트 조회 성공")
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<CalendarEventResponse>> getMonthlyEvents(
            @Parameter(description = "년도") @PathVariable int year,
            @Parameter(description = "월 (1-12)") @PathVariable int month,
            Authentication authentication) {
        
        log.debug("월별 이벤트 조회: 사용자={}, 년도={}, 월={}", authentication.getName(), year, month);
        
        List<CalendarEventResponse> responses = calendarEventService.getMonthlyEvents(
                authentication.getName(), year, month);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "주별 이벤트 조회", description = "지정된 주의 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주별 이벤트 조회 성공")
    @GetMapping("/week/{year}/{week}")
    public ResponseEntity<List<CalendarEventResponse>> getWeeklyEvents(
            @Parameter(description = "년도") @PathVariable int year,
            @Parameter(description = "주차 (1-53)") @PathVariable int week,
            Authentication authentication) {
        
        log.debug("주별 이벤트 조회: 사용자={}, 년도={}, 주차={}", authentication.getName(), year, week);
        
        List<CalendarEventResponse> responses = calendarEventService.getWeeklyEvents(
                authentication.getName(), year, week);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "일별 이벤트 조회", description = "지정된 날짜의 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "일별 이벤트 조회 성공")
    @GetMapping("/daily/{date}")
    public ResponseEntity<List<CalendarEventResponse>> getDailyEvents(
            @Parameter(description = "날짜 (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        
        log.debug("일별 이벤트 조회: 사용자={}, 날짜={}", authentication.getName(), date);
        
        List<CalendarEventResponse> responses = calendarEventService.getDailyEvents(authentication.getName(), date);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "이벤트 상태 변경", description = "이벤트의 상태를 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @PatchMapping("/{eventId}/status")
    public ResponseEntity<CalendarEventResponse> updateEventStatus(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "새로운 상태") @RequestParam EventStatus status,
            Authentication authentication) {
        
        log.info("이벤트 상태 변경: 사용자={}, 이벤트ID={}, 상태={}", 
                authentication.getName(), eventId, status);
        
        CalendarEventResponse response = calendarEventService.updateEventStatus(
                authentication.getName(), eventId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 이벤트 조회", description = "지정된 카테고리의 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리별 이벤트 조회 성공")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CalendarEventResponse>> getEventsByCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            Authentication authentication) {
        
        log.debug("카테고리별 이벤트 조회: 사용자={}, 카테고리ID={}", authentication.getName(), categoryId);
        
        List<CalendarEventResponse> responses = calendarEventService.getEventsByCategory(
                authentication.getName(), categoryId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "겹치는 이벤트 확인", description = "지정된 시간 범위와 겹치는 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "겹치는 이벤트 조회 성공")
    @GetMapping("/overlapping")
    public ResponseEntity<List<CalendarEventResponse>> getOverlappingEvents(
            @Parameter(description = "시작 시간")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "종료 시간")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "제외할 이벤트 ID (선택사항)")
            @RequestParam(required = false) Long excludeEventId,
            Authentication authentication) {
        
        log.debug("겹치는 이벤트 조회: 사용자={}, 시작={}, 종료={}", 
                authentication.getName(), startTime, endTime);
        
        List<CalendarEventResponse> responses = calendarEventService.getOverlappingEvents(
                authentication.getName(), startTime, endTime, excludeEventId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공유된 이벤트 조회", description = "사용자와 공유된 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공유된 이벤트 조회 성공")
    @GetMapping("/shared")
    public ResponseEntity<List<CalendarEventResponse>> getSharedEvents(Authentication authentication) {
        log.debug("공유된 이벤트 조회: 사용자={}", authentication.getName());
        
        List<CalendarEventResponse> responses = calendarEventService.getSharedEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공유된 특정 이벤트 조회", description = "공유된 특정 이벤트의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "공유된 이벤트 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "공유된 이벤트를 찾을 수 없음")
    })
    @GetMapping("/shared/{eventId}")
    public ResponseEntity<CalendarEventResponse> getSharedEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            Authentication authentication) {
        
        log.debug("공유된 특정 이벤트 조회: 사용자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        CalendarEventResponse response = calendarEventService.getSharedEvent(authentication.getName(), eventId);
        return ResponseEntity.ok(response);
    }
}