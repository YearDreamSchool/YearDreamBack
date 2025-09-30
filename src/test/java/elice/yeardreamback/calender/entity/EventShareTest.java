package elice.yeardreamback.calender.entity;

import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.exception.EventSharingException;
import elice.yeardreamback.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * EventShare 엔티티 테스트
 */
class EventShareTest {

    private CalendarEvent testEvent;
    private User eventOwner;
    private User sharedUser;

    @BeforeEach
    void setUp() {
        eventOwner = new User();
        eventOwner.setId(1L);
        eventOwner.setUsername("owner");
        eventOwner.setName("이벤트 소유자");

        sharedUser = new User();
        sharedUser.setId(2L);
        sharedUser.setUsername("shared");
        sharedUser.setName("공유받는 사용자");

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);

        testEvent = new CalendarEvent(
            eventOwner,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            null
        );
    }

    @Test
    @DisplayName("유효한 이벤트 공유 생성 테스트")
    void createValidEventShare() {
        // given & when
        EventShare share = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);

        // then
        assertThatNoException().isThrownBy(share::validateShare);
        assertThat(share.getEvent()).isEqualTo(testEvent);
        assertThat(share.getSharedWithUser()).isEqualTo(sharedUser);
        assertThat(share.getPermission()).isEqualTo(SharePermission.VIEW_ONLY);
    }

    @Test
    @DisplayName("권한 확인 테스트")
    void checkPermissions() {
        // given
        EventShare viewOnlyShare = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);
        EventShare editShare = new EventShare(testEvent, sharedUser, SharePermission.EDIT);

        // when & then - VIEW_ONLY 권한
        assertThat(viewOnlyShare.canView()).isTrue();
        assertThat(viewOnlyShare.canEdit()).isFalse();

        // when & then - EDIT 권한
        assertThat(editShare.canView()).isTrue();
        assertThat(editShare.canEdit()).isTrue();
    }

    @Test
    @DisplayName("이벤트가 null인 경우 예외 발생")
    void throwExceptionWhenEventIsNull() {
        // given
        EventShare share = new EventShare(null, sharedUser, SharePermission.VIEW_ONLY);

        // when & then
        assertThatThrownBy(share::validateShare)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("공유할 이벤트는 필수입니다");
    }

    @Test
    @DisplayName("공유받을 사용자가 null인 경우 예외 발생")
    void throwExceptionWhenSharedUserIsNull() {
        // given
        EventShare share = new EventShare(testEvent, null, SharePermission.VIEW_ONLY);

        // when & then
        assertThatThrownBy(share::validateShare)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("공유받을 사용자는 필수입니다");
    }

    @Test
    @DisplayName("권한이 null인 경우 예외 발생")
    void throwExceptionWhenPermissionIsNull() {
        // given
        EventShare share = new EventShare(testEvent, sharedUser, null);

        // when & then
        assertThatThrownBy(share::validateShare)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("공유 권한은 필수입니다");
    }

    @Test
    @DisplayName("자기 자신과 공유하려는 경우 예외 발생")
    void throwExceptionWhenSharingWithSelf() {
        // given
        EventShare share = new EventShare(testEvent, eventOwner, SharePermission.VIEW_ONLY);

        // when & then
        assertThatThrownBy(share::validateShare)
            .isInstanceOf(EventSharingException.class)
            .hasMessage("자기 자신과는 이벤트를 공유할 수 없습니다");
    }

    @Test
    @DisplayName("권한 변경 테스트")
    void changePermission() {
        // given
        EventShare share = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);

        // when
        share.changePermission(SharePermission.EDIT);

        // then
        assertThat(share.getPermission()).isEqualTo(SharePermission.EDIT);
        assertThat(share.canEdit()).isTrue();
    }

    @Test
    @DisplayName("null 권한으로 변경하려는 경우 예외 발생")
    void throwExceptionWhenChangingToNullPermission() {
        // given
        EventShare share = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);

        // when & then
        assertThatThrownBy(() -> share.changePermission(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("새로운 권한은 필수입니다");
    }

    @Test
    @DisplayName("소유자 확인 테스트")
    void isOwnedByTest() {
        // given
        EventShare share = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);

        // when & then
        assertThat(share.isOwnedBy(eventOwner)).isTrue();
        assertThat(share.isOwnedBy(sharedUser)).isFalse();
    }

    @Test
    @DisplayName("공유받은 사용자 확인 테스트")
    void isSharedWithTest() {
        // given
        EventShare share = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);

        // when & then
        assertThat(share.isSharedWith(sharedUser)).isTrue();
        assertThat(share.isSharedWith(eventOwner)).isFalse();
    }

    @Test
    @DisplayName("이벤트 소유자가 null인 경우 소유자 확인")
    void isOwnedByWhenEventOwnerIsNull() {
        // given
        CalendarEvent eventWithoutOwner = new CalendarEvent();
        EventShare share = new EventShare(eventWithoutOwner, sharedUser, SharePermission.VIEW_ONLY);

        // when & then
        assertThat(share.isOwnedBy(eventOwner)).isFalse();
    }
}