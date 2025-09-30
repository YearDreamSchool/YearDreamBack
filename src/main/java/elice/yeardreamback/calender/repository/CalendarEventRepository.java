package elice.yeardreamback.calender.repository;

import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CalendarEvent 엔티티를 위한 리포지토리 인터페이스
 * 캘린더 이벤트 데이터 접근을 담당
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    /**
     * 사용자명으로 모든 이벤트 조회
     */
    List<CalendarEvent> findByUserUsernameOrderByStartTimeAsc(String username);

    /**
     * 사용자명과 날짜 범위로 이벤트 조회
     */
    List<CalendarEvent> findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(
            String username, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자명과 이벤트 ID로 특정 이벤트 조회 (소유권 확인)
     */
    Optional<CalendarEvent> findByIdAndUserUsername(Long id, String username);

    /**
     * 사용자명과 이벤트 상태로 이벤트 조회
     */
    List<CalendarEvent> findByUserUsernameAndStatusOrderByStartTimeAsc(String username, EventStatus status);

    /**
     * 사용자명과 카테고리 ID로 이벤트 조회
     */
    List<CalendarEvent> findByUserUsernameAndCategoryIdOrderByStartTimeAsc(String username, Long categoryId);

    /**
     * 특정 기간 내 이벤트 개수 조회
     */
    @Query("SELECT COUNT(e) FROM CalendarEvent e WHERE e.user.username = :username " +
           "AND e.startTime >= :startDate AND e.endTime <= :endDate")
    long countByUserUsernameAndDateRange(@Param("username") String username,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 특정 날짜 이벤트 조회
     */
    @Query("SELECT e FROM CalendarEvent e WHERE e.user.username = :username " +
           "AND DATE(e.startTime) = DATE(:date) ORDER BY e.startTime ASC")
    List<CalendarEvent> findByUserUsernameAndDate(@Param("username") String username,
                                                  @Param("date") LocalDateTime date);

    /**
     * 겹치는 시간대의 이벤트 조회 (새 이벤트 생성 시 충돌 확인용)
     */
    @Query("SELECT e FROM CalendarEvent e WHERE e.user.username = :username " +
           "AND ((e.startTime <= :endTime AND e.endTime >= :startTime)) " +
           "AND (:eventId IS NULL OR e.id != :eventId)")
    List<CalendarEvent> findOverlappingEvents(@Param("username") String username,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("eventId") Long eventId);

    /**
     * 사용자와 공유된 이벤트 조회
     */
    @Query("SELECT e FROM CalendarEvent e JOIN e.shares s WHERE s.sharedWithUser.username = :username " +
           "ORDER BY e.startTime ASC")
    List<CalendarEvent> findSharedEvents(@Param("username") String username);

    /**
     * 특정 사용자와 공유된 특정 이벤트 조회
     */
    @Query("SELECT e FROM CalendarEvent e JOIN e.shares s WHERE e.id = :eventId " +
           "AND s.sharedWithUser.username = :username")
    Optional<CalendarEvent> findSharedEventById(@Param("eventId") Long eventId,
                                               @Param("username") String username);
}