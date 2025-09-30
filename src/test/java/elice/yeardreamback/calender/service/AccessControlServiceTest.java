package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.exception.AccessDeniedException;
import elice.yeardreamback.calender.exception.CategoryNotFoundException;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.repository.EventShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * AccessControlService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("접근 제어 서비스 테스트")
class AccessControlServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private EventShareRepository eventShareRepository;

    @InjectMocks
    private AccessControlService accessControlService;

    private CalendarEvent testEvent;
    private EventCategory testCategory;
    private EventShare testEventShare;

    @BeforeEach
    void setUp() {
        testEvent = CalendarEvent.builder()
                .id(1L)
                .title("테스트 이벤트")
                .description("테스트 설명")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(EventStatus.SCHEDULED)
                .ownerUsername("owner")
                .build();

        testCategory = EventCategory.builder()
                .id(1L)
                .name("테스트 카테고리")
                .color("#FF0000")
                .ownerUsername("owner")
                .build();

        testEventShare = EventShare.builder()
                .id(1L)
                .eventId(1L)
                .sharedByUsername("owner")
                .sharedWithUsername("shared_user")
                .permission(SharePermission.VIEW_ONLY)
                .build();
    }

    @Test
    @DisplayName("이벤트 읽기 권한 - 소유자 접근 성공")
    void verifyEventReadAccess_Owner_Success() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        CalendarEvent result = accessControlService.verifyEventReadAccess("owner", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 읽기 권한 - 공유받은 사용자 접근 성공")
    void verifyEventReadAccess_SharedUser_Success() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared_user"))
                .willReturn(Optional.of(testEventShare));

        // when
        CalendarEvent result = accessControlService.verifyEventReadAccess("shared_user", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 읽기 권한 - 권한 없는 사용자 접근 실패")
    void verifyEventReadAccess_UnauthorizedUser_Failure() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "unauthorized_user"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyEventReadAccess("unauthorized_user", 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("이벤트에 대한 접근 권한이 없습니다");
    }

    @Test
    @DisplayName("이벤트 읽기 권한 - 존재하지 않는 이벤트")
    void verifyEventReadAccess_EventNotFound() {
        // given
        given(calendarEventRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyEventReadAccess("owner", 999L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("이벤트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트 편집 권한 - 소유자 접근 성공")
    void verifyEventEditAccess_Owner_Success() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        CalendarEvent result = accessControlService.verifyEventEditAccess("owner", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 편집 권한 - 편집 권한이 있는 공유 사용자 접근 성공")
    void verifyEventEditAccess_SharedUserWithEditPermission_Success() {
        // given
        testEventShare.setPermission(SharePermission.EDIT);
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared_user"))
                .willReturn(Optional.of(testEventShare));

        // when
        CalendarEvent result = accessControlService.verifyEventEditAccess("shared_user", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 편집 권한 - 읽기 전용 공유 사용자 접근 실패")
    void verifyEventEditAccess_SharedUserWithViewOnlyPermission_Failure() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared_user"))
                .willReturn(Optional.of(testEventShare)); // VIEW_ONLY 권한

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyEventEditAccess("shared_user", 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("이벤트에 대한 편집 권한이 없습니다");
    }

    @Test
    @DisplayName("이벤트 삭제 권한 - 소유자만 삭제 가능")
    void verifyEventDeleteAccess_Owner_Success() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        CalendarEvent result = accessControlService.verifyEventDeleteAccess("owner", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 삭제 권한 - 소유자가 아닌 사용자 삭제 실패")
    void verifyEventDeleteAccess_NonOwner_Failure() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyEventDeleteAccess("other_user", 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("이벤트 삭제는 소유자만 가능합니다");
    }

    @Test
    @DisplayName("카테고리 접근 권한 - 소유자 접근 성공")
    void verifyCategoryAccess_Owner_Success() {
        // given
        given(eventCategoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        // when
        EventCategory result = accessControlService.verifyCategoryAccess("owner", 1L);

        // then
        assertThat(result).isEqualTo(testCategory);
    }

    @Test
    @DisplayName("카테고리 접근 권한 - 소유자가 아닌 사용자 접근 실패")
    void verifyCategoryAccess_NonOwner_Failure() {
        // given
        given(eventCategoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyCategoryAccess("other_user", 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("카테고리에 대한 접근 권한이 없습니다");
    }

    @Test
    @DisplayName("카테고리 접근 권한 - 존재하지 않는 카테고리")
    void verifyCategoryAccess_CategoryNotFound() {
        // given
        given(eventCategoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyCategoryAccess("owner", 999L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트 공유 권한 - 소유자만 공유 가능")
    void verifyEventShareAccess_Owner_Success() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        CalendarEvent result = accessControlService.verifyEventShareAccess("owner", 1L);

        // then
        assertThat(result).isEqualTo(testEvent);
    }

    @Test
    @DisplayName("이벤트 공유 권한 - 소유자가 아닌 사용자 공유 실패")
    void verifyEventShareAccess_NonOwner_Failure() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when & then
        assertThatThrownBy(() -> accessControlService.verifyEventShareAccess("other_user", 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("이벤트 공유는 소유자만 가능합니다");
    }

    @Test
    @DisplayName("이벤트 편집 권한 확인 - 권한 있음")
    void hasEventEditPermission_WithPermission_ReturnsTrue() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        boolean result = accessControlService.hasEventEditPermission("owner", 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이벤트 편집 권한 확인 - 권한 없음")
    void hasEventEditPermission_WithoutPermission_ReturnsFalse() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "other_user"))
                .willReturn(Optional.empty());

        // when
        boolean result = accessControlService.hasEventEditPermission("other_user", 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이벤트 읽기 권한 확인 - 권한 있음")
    void hasEventReadPermission_WithPermission_ReturnsTrue() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));

        // when
        boolean result = accessControlService.hasEventReadPermission("owner", 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이벤트 읽기 권한 확인 - 권한 없음")
    void hasEventReadPermission_WithoutPermission_ReturnsFalse() {
        // given
        given(calendarEventRepository.findById(1L)).willReturn(Optional.of(testEvent));
        given(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "other_user"))
                .willReturn(Optional.empty());

        // when
        boolean result = accessControlService.hasEventReadPermission("other_user", 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("카테고리 접근 권한 확인 - 권한 있음")
    void hasCategoryAccess_WithPermission_ReturnsTrue() {
        // given
        given(eventCategoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        // when
        boolean result = accessControlService.hasCategoryAccess("owner", 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("카테고리 접근 권한 확인 - 권한 없음")
    void hasCategoryAccess_WithoutPermission_ReturnsFalse() {
        // given
        given(eventCategoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        // when
        boolean result = accessControlService.hasCategoryAccess("other_user", 1L);

        // then
        assertThat(result).isFalse();
    }
}