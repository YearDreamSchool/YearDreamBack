package elice.yeardreamback.calender.entity;

import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.exception.InvalidEventTimeException;
import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * CalendarEvent 엔티티 유효성 검사 테스트
 */
class CalendarEventTest {

    private User testUser;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        validStartTime = LocalDateTime.now().plusHours(1);
        validEndTime = validStartTime.plusHours(2);
    }

    @Test
    @DisplayName("유효한 이벤트 생성 테스트")
    void createValidEvent() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatNoException().isThrownBy(event::validateEvent);
        assertThat(event.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(event.getStatus()).isEqualTo(EventStatus.SCHEDULED);
        assertThat(event.isValidTimeRange()).isTrue();
    }

    @Test
    @DisplayName("제목이 null인 경우 예외 발생")
    void throwExceptionWhenTitleIsNull() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            null,
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트 제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 빈 문자열인 경우 예외 발생")
    void throwExceptionWhenTitleIsEmpty() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "   ",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트 제목은 필수입니다");
    }

    @Test
    @DisplayName("시작 시간이 null인 경우 예외 발생")
    void throwExceptionWhenStartTimeIsNull() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            null,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트 시작 시간은 필수입니다");
    }

    @Test
    @DisplayName("종료 시간이 null인 경우 예외 발생")
    void throwExceptionWhenEndTimeIsNull() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            null,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트 종료 시간은 필수입니다");
    }

    @Test
    @DisplayName("사용자가 null인 경우 예외 발생")
    void throwExceptionWhenUserIsNull() {
        // given
        CalendarEvent event = new CalendarEvent(
            null,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트 소유자는 필수입니다");
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦은 경우 예외 발생")
    void throwExceptionWhenStartTimeIsAfterEndTime() {
        // given
        LocalDateTime invalidStartTime = validEndTime.plusHours(1);
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            invalidStartTime,
            validEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(InvalidEventTimeException.class)
            .hasMessage("이벤트 시작 시간은 종료 시간보다 이전이어야 합니다");
    }

    @Test
    @DisplayName("이벤트 기간이 7일을 초과하는 경우 예외 발생")
    void throwExceptionWhenEventDurationExceedsSevenDays() {
        // given
        LocalDateTime longEndTime = validStartTime.plusDays(8);
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            longEndTime,
            "테스트 장소",
            null
        );

        // when & then
        assertThatThrownBy(event::validateEvent)
            .isInstanceOf(InvalidEventTimeException.class)
            .hasMessage("이벤트 기간은 최대 7일을 초과할 수 없습니다");
    }

    @Test
    @DisplayName("알림 추가 및 제거 테스트")
    void addAndRemoveReminder() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );
        EventReminder reminder = new EventReminder(event, 30);

        // when
        event.addReminder(reminder);

        // then
        assertThat(event.getReminders()).hasSize(1);
        assertThat(event.getReminders().get(0)).isEqualTo(reminder);
        assertThat(reminder.getEvent()).isEqualTo(event);

        // when - 알림 제거
        event.removeReminder(reminder);

        // then
        assertThat(event.getReminders()).isEmpty();
        assertThat(reminder.getEvent()).isNull();
    }

    @Test
    @DisplayName("공유 추가 및 제거 테스트")
    void addAndRemoveShare() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            null
        );
        
        User sharedUser = new User();
        sharedUser.setId(2L);
        sharedUser.setUsername("shareduser");
        
        EventShare share = new EventShare(event, sharedUser, 
            elice.yeardreamback.calender.enums.SharePermission.VIEW_ONLY);

        // when
        event.addShare(share);

        // then
        assertThat(event.getShares()).hasSize(1);
        assertThat(event.getShares().get(0)).isEqualTo(share);
        assertThat(share.getEvent()).isEqualTo(event);

        // when - 공유 제거
        event.removeShare(share);

        // then
        assertThat(event.getShares()).isEmpty();
        assertThat(share.getEvent()).isNull();
    }
}