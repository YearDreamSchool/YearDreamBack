package elice.yeardreamback.calender.entity;

import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * EventReminder 엔티티 테스트
 */
class EventReminderTest {

    private CalendarEvent testEvent;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);

        testEvent = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            null
        );
    }

    @Test
    @DisplayName("유효한 알림 생성 테스트")
    void createValidReminder() {
        // given & when
        EventReminder reminder = new EventReminder(testEvent, 30);

        // then
        assertThatNoException().isThrownBy(reminder::validateReminder);
        assertThat(reminder.getEvent()).isEqualTo(testEvent);
        assertThat(reminder.getMinutesBefore()).isEqualTo(30);
        assertThat(reminder.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("알림 시간 계산 테스트")
    void calculateReminderTime() {
        // given
        EventReminder reminder = new EventReminder(testEvent, 30);

        // when
        LocalDateTime reminderTime = reminder.getReminderTime();

        // then
        assertThat(reminderTime).isEqualTo(testEvent.getStartTime().minusMinutes(30));
    }

    @Test
    @DisplayName("이벤트가 null인 경우 예외 발생")
    void throwExceptionWhenEventIsNull() {
        // given
        EventReminder reminder = new EventReminder(null, 30);

        // when & then
        assertThatThrownBy(reminder::validateReminder)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("알림이 설정될 이벤트는 필수입니다");
    }

    @Test
    @DisplayName("알림 시간이 null인 경우 예외 발생")
    void throwExceptionWhenMinutesBeforeIsNull() {
        // given
        EventReminder reminder = new EventReminder(testEvent, null);

        // when & then
        assertThatThrownBy(reminder::validateReminder)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("알림 시간은 필수입니다");
    }

    @Test
    @DisplayName("알림 시간이 음수인 경우 예외 발생")
    void throwExceptionWhenMinutesBeforeIsNegative() {
        // given
        EventReminder reminder = new EventReminder(testEvent, -10);

        // when & then
        assertThatThrownBy(reminder::validateReminder)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("알림 시간은 0분 이상이어야 합니다");
    }

    @Test
    @DisplayName("알림 시간이 1주일을 초과하는 경우 예외 발생")
    void throwExceptionWhenMinutesBeforeExceedsOneWeek() {
        // given
        EventReminder reminder = new EventReminder(testEvent, 10081); // 1주일 + 1분

        // when & then
        assertThatThrownBy(reminder::validateReminder)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("알림은 최대 1주일 전까지만 설정할 수 있습니다");
    }

    @Test
    @DisplayName("알림 활성화/비활성화 테스트")
    void activateAndDeactivateReminder() {
        // given
        EventReminder reminder = new EventReminder(testEvent, 30);

        // when - 비활성화
        reminder.deactivate();

        // then
        assertThat(reminder.getIsActive()).isFalse();

        // when - 활성화
        reminder.activate();

        // then
        assertThat(reminder.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("미래 알림인지 확인 테스트")
    void isUpcomingTest() {
        // given - 미래 이벤트
        LocalDateTime futureStartTime = LocalDateTime.now().plusHours(2);
        LocalDateTime futureEndTime = futureStartTime.plusHours(1);
        CalendarEvent futureEvent = new CalendarEvent(
            testUser,
            "미래 이벤트",
            "설명",
            futureStartTime,
            futureEndTime,
            "장소",
            null
        );
        EventReminder futureReminder = new EventReminder(futureEvent, 30);

        // when & then
        assertThat(futureReminder.isUpcoming()).isTrue();

        // given - 과거 이벤트
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(2);
        LocalDateTime pastEndTime = pastStartTime.plusHours(1);
        CalendarEvent pastEvent = new CalendarEvent(
            testUser,
            "과거 이벤트",
            "설명",
            pastStartTime,
            pastEndTime,
            "장소",
            null
        );
        EventReminder pastReminder = new EventReminder(pastEvent, 30);

        // when & then
        assertThat(pastReminder.isUpcoming()).isFalse();
    }

    @Test
    @DisplayName("이벤트 시작 시간이 null인 경우 알림 시간도 null")
    void reminderTimeIsNullWhenEventStartTimeIsNull() {
        // given
        CalendarEvent eventWithoutStartTime = new CalendarEvent();
        EventReminder reminder = new EventReminder(eventWithoutStartTime, 30);

        // when
        LocalDateTime reminderTime = reminder.getReminderTime();

        // then
        assertThat(reminderTime).isNull();
    }
}