package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventReminderResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 알림 서비스 인터페이스
 * 알림 관리 및 비즈니스 로직을 담당
 */
public interface EventReminderService {

    /**
     * 특정 이벤트의 모든 알림 조회
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 알림 목록
     */
    List<EventReminderResponse> getEventReminders(String username, Long eventId);

    /**
     * 특정 이벤트의 활성화된 알림만 조회
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 활성화된 알림 목록
     */
    List<EventReminderResponse> getActiveEventReminders(String username, Long eventId);

    /**
     * 사용자의 모든 활성화된 알림 조회
     * 
     * @param username 사용자명
     * @return 활성화된 알림 목록
     */
    List<EventReminderResponse> getUserActiveReminders(String username);

    /**
     * 특정 날짜의 알림 조회
     * 
     * @param username 사용자명
     * @param date 날짜
     * @return 해당 날짜의 알림 목록
     */
    List<EventReminderResponse> getDailyReminders(String username, LocalDate date);

    /**
     * 알림 활성화
     * 
     * @param username 사용자명
     * @param reminderId 알림 ID
     * @return 수정된 알림 응답
     */
    EventReminderResponse activateReminder(String username, Long reminderId);

    /**
     * 알림 비활성화
     * 
     * @param username 사용자명
     * @param reminderId 알림 ID
     * @return 수정된 알림 응답
     */
    EventReminderResponse deactivateReminder(String username, Long reminderId);

    /**
     * 특정 시간 범위 내에 발송될 알림 조회 (알림 시스템용)
     * 
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 발송 대상 알림 목록
     */
    List<EventReminderResponse> getRemindersToSend(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 특정 이벤트 시간과 알림 시간으로 알림 조회
     * 
     * @param eventStartTime 이벤트 시작 시간
     * @param minutesBefore 알림 시간 (분)
     * @return 해당 조건의 알림 목록
     */
    List<EventReminderResponse> getRemindersByEventTimeAndMinutes(LocalDateTime eventStartTime, Integer minutesBefore);

    /**
     * 과거 이벤트의 알림 조회 (정리용)
     * 
     * @param currentTime 현재 시간
     * @return 과거 이벤트의 알림 목록
     */
    List<EventReminderResponse> getRemindersForPastEvents(LocalDateTime currentTime);

    /**
     * 특정 이벤트의 알림 개수 조회
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 알림 개수
     */
    long getEventReminderCount(String username, Long eventId);

    /**
     * 사용자의 총 알림 개수 조회
     * 
     * @param username 사용자명
     * @return 총 알림 개수
     */
    long getUserReminderCount(String username);

    /**
     * 미래 알림 개수 조회 (아직 발송되지 않은 알림)
     * 
     * @param username 사용자명
     * @return 미래 알림 개수
     */
    long getUpcomingReminderCount(String username);

    /**
     * 과거 이벤트의 알림 정리 (배치 작업용)
     * 
     * @param beforeDate 이 날짜 이전의 이벤트 알림 삭제
     * @return 삭제된 알림 개수
     */
    long cleanupPastReminders(LocalDateTime beforeDate);
}