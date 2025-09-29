package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.enums.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 이벤트 서비스 인터페이스
 * 이벤트 CRUD 및 비즈니스 로직을 담당
 */
public interface CalendarEventService {

    /**
     * 새로운 이벤트 생성
     * 
     * @param username 사용자명
     * @param request 이벤트 생성 요청
     * @return 생성된 이벤트 응답
     */
    CalendarEventResponse createEvent(String username, CalendarEventRequest request);

    /**
     * 기존 이벤트 수정
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @param request 이벤트 수정 요청
     * @return 수정된 이벤트 응답
     */
    CalendarEventResponse updateEvent(String username, Long eventId, CalendarEventRequest request);

    /**
     * 이벤트 삭제
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     */
    void deleteEvent(String username, Long eventId);

    /**
     * 특정 이벤트 조회
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 이벤트 응답
     */
    CalendarEventResponse getEvent(String username, Long eventId);

    /**
     * 사용자의 모든 이벤트 조회
     * 
     * @param username 사용자명
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getUserEvents(String username);

    /**
     * 날짜 범위로 이벤트 조회
     * 
     * @param username 사용자명
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getUserEvents(String username, LocalDate startDate, LocalDate endDate);

    /**
     * 월별 이벤트 조회
     * 
     * @param username 사용자명
     * @param year 년도
     * @param month 월 (1-12)
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getMonthlyEvents(String username, int year, int month);

    /**
     * 주별 이벤트 조회
     * 
     * @param username 사용자명
     * @param year 년도
     * @param week 주차 (1-53)
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getWeeklyEvents(String username, int year, int week);

    /**
     * 특정 날짜의 이벤트 조회
     * 
     * @param username 사용자명
     * @param date 날짜
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getDailyEvents(String username, LocalDate date);

    /**
     * 이벤트 상태 변경
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @param status 새로운 상태
     * @return 수정된 이벤트 응답
     */
    CalendarEventResponse updateEventStatus(String username, Long eventId, EventStatus status);

    /**
     * 카테고리별 이벤트 조회
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     * @return 이벤트 목록
     */
    List<CalendarEventResponse> getEventsByCategory(String username, Long categoryId);

    /**
     * 겹치는 이벤트 확인
     * 
     * @param username 사용자명
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param excludeEventId 제외할 이벤트 ID (수정 시 사용)
     * @return 겹치는 이벤트 목록
     */
    List<CalendarEventResponse> getOverlappingEvents(String username, LocalDateTime startTime, 
                                                   LocalDateTime endTime, Long excludeEventId);

    /**
     * 사용자와 공유된 이벤트 조회
     * 
     * @param username 사용자명
     * @return 공유된 이벤트 목록
     */
    List<CalendarEventResponse> getSharedEvents(String username);

    /**
     * 특정 공유 이벤트 조회
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 공유된 이벤트 응답
     */
    CalendarEventResponse getSharedEvent(String username, Long eventId);
}