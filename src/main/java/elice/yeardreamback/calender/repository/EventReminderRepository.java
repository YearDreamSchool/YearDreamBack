package elice.yeardreamback.calender.repository;

import elice.yeardreamback.calender.entity.EventReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EventReminder 엔티티를 위한 리포지토리 인터페이스
 * 이벤트 알림 데이터 접근을 담당
 */
@Repository
public interface EventReminderRepository extends JpaRepository<EventReminder, Long> {

    /**
     * 특정 이벤트의 모든 알림 조회
     */
    List<EventReminder> findByEventIdOrderByMinutesBeforeAsc(Long eventId);

    /**
     * 특정 이벤트의 활성화된 알림만 조회
     */
    List<EventReminder> findByEventIdAndIsActiveTrueOrderByMinutesBeforeAsc(Long eventId);

    /**
     * 사용자의 모든 활성화된 알림 조회
     */
    @Query("SELECT r FROM EventReminder r WHERE r.event.user.username = :username " +
           "AND r.isActive = true ORDER BY r.event.startTime ASC, r.minutesBefore ASC")
    List<EventReminder> findActiveRemindersByUsername(@Param("username") String username);

    /**
     * 특정 시간 범위 내에 발송될 알림 조회 (알림 시스템용)
     */
    @Query("SELECT r FROM EventReminder r WHERE r.isActive = true " +
           "AND r.event.startTime BETWEEN :startTime AND :endTime " +
           "ORDER BY r.event.startTime ASC, r.minutesBefore ASC")
    List<EventReminder> findRemindersToSend(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 시간에 발송될 알림 조회
     */
    @Query("SELECT r FROM EventReminder r WHERE r.isActive = true " +
           "AND r.event.startTime = :eventStartTime " +
           "AND r.minutesBefore = :minutesBefore")
    List<EventReminder> findRemindersByEventTimeAndMinutes(@Param("eventStartTime") LocalDateTime eventStartTime,
                                                          @Param("minutesBefore") Integer minutesBefore);

    /**
     * 사용자의 특정 날짜 알림 조회
     */
    @Query("SELECT r FROM EventReminder r WHERE r.event.user.username = :username " +
           "AND r.isActive = true AND DATE(r.event.startTime) = DATE(:date) " +
           "ORDER BY r.event.startTime ASC, r.minutesBefore ASC")
    List<EventReminder> findRemindersByUsernameAndDate(@Param("username") String username,
                                                      @Param("date") LocalDateTime date);

    /**
     * 과거 이벤트의 알림 조회 (정리용)
     */
    @Query("SELECT r FROM EventReminder r WHERE r.event.endTime < :currentTime")
    List<EventReminder> findRemindersForPastEvents(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 특정 이벤트의 알림 개수 조회
     */
    long countByEventId(Long eventId);

    /**
     * 사용자의 총 알림 개수 조회
     */
    @Query("SELECT COUNT(r) FROM EventReminder r WHERE r.event.user.username = :username")
    long countByUsername(@Param("username") String username);
}