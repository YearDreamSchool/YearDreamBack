package elice.yeardreamback.calender.repository;

import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CalendarEventRepository 테스트
 */
@DataJpaTest
class CalendarEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    private User testUser;
    private User otherUser;
    private EventCategory testCategory;
    private CalendarEvent testEvent;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");
        testUser.setEmail("test@example.com");
        entityManager.persistAndFlush(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setName("다른 사용자");
        otherUser.setEmail("other@example.com");
        entityManager.persistAndFlush(otherUser);

        // 테스트 카테고리 생성
        testCategory = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        entityManager.persistAndFlush(testCategory);

        // 테스트 이벤트 생성
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        testEvent = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            testCategory
        );
        entityManager.persistAndFlush(testEvent);
    }

    @Test
    @DisplayName("사용자명으로 이벤트 조회 테스트")
    void findByUserUsernameOrderByStartTimeAsc() {
        // when
        List<CalendarEvent> events = calendarEventRepository.findByUserUsernameOrderByStartTimeAsc("testuser");

        // then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("테스트 이벤트");
        assertThat(events.get(0).getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("사용자명과 이벤트 ID로 특정 이벤트 조회 테스트")
    void findByIdAndUserUsername() {
        // when
        Optional<CalendarEvent> foundEvent = calendarEventRepository.findByIdAndUserUsername(
            testEvent.getId(), "testuser");

        // then
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getTitle()).isEqualTo("테스트 이벤트");

        // when - 다른 사용자로 조회
        Optional<CalendarEvent> notFoundEvent = calendarEventRepository.findByIdAndUserUsername(
            testEvent.getId(), "otheruser");

        // then
        assertThat(notFoundEvent).isEmpty();
    }

    @Test
    @DisplayName("날짜 범위로 이벤트 조회 테스트")
    void findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc() {
        // given
        LocalDateTime searchStart = LocalDateTime.now();
        LocalDateTime searchEnd = LocalDateTime.now().plusDays(1);

        // when
        List<CalendarEvent> events = calendarEventRepository
            .findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(
                "testuser", searchStart, searchEnd);

        // then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("테스트 이벤트");
    }

    @Test
    @DisplayName("이벤트 상태로 조회 테스트")
    void findByUserUsernameAndStatusOrderByStartTimeAsc() {
        // when
        List<CalendarEvent> scheduledEvents = calendarEventRepository
            .findByUserUsernameAndStatusOrderByStartTimeAsc("testuser", EventStatus.SCHEDULED);

        // then
        assertThat(scheduledEvents).hasSize(1);
        assertThat(scheduledEvents.get(0).getStatus()).isEqualTo(EventStatus.SCHEDULED);
    }

    @Test
    @DisplayName("카테고리 ID로 이벤트 조회 테스트")
    void findByUserUsernameAndCategoryIdOrderByStartTimeAsc() {
        // when
        List<CalendarEvent> events = calendarEventRepository
            .findByUserUsernameAndCategoryIdOrderByStartTimeAsc("testuser", testCategory.getId());

        // then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    @DisplayName("겹치는 이벤트 조회 테스트")
    void findOverlappingEvents() {
        // given - 겹치는 시간대의 새 이벤트 시간
        LocalDateTime newStartTime = testEvent.getStartTime().plusMinutes(30);
        LocalDateTime newEndTime = testEvent.getEndTime().plusMinutes(30);

        // when
        List<CalendarEvent> overlappingEvents = calendarEventRepository.findOverlappingEvents(
            "testuser", newStartTime, newEndTime, null);

        // then
        assertThat(overlappingEvents).hasSize(1);
        assertThat(overlappingEvents.get(0).getId()).isEqualTo(testEvent.getId());

        // when - 기존 이벤트 ID 제외하고 조회
        List<CalendarEvent> overlappingEventsExcluding = calendarEventRepository.findOverlappingEvents(
            "testuser", newStartTime, newEndTime, testEvent.getId());

        // then
        assertThat(overlappingEventsExcluding).isEmpty();
    }

    @Test
    @DisplayName("특정 기간 내 이벤트 개수 조회 테스트")
    void countByUserUsernameAndDateRange() {
        // given
        LocalDateTime rangeStart = LocalDateTime.now();
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(1);

        // when
        long count = calendarEventRepository.countByUserUsernameAndDateRange(
            "testuser", rangeStart, rangeEnd);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 조회 시 빈 결과")
    void findByNonExistentUser() {
        // when
        List<CalendarEvent> events = calendarEventRepository
            .findByUserUsernameOrderByStartTimeAsc("nonexistent");

        // then
        assertThat(events).isEmpty();
    }
}