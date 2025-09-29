package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventShareRequest;
import elice.yeardreamback.calender.dto.EventShareResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.exception.EventSharingException;
import elice.yeardreamback.calender.mapper.EventShareMapper;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventShareRepository;
import elice.yeardreamback.calender.service.impl.EventSharingServiceImpl;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EventSharingService 테스트
 */
@ExtendWith(MockitoExtension.class)
class EventSharingServiceTest {

    @Mock
    private EventShareRepository eventShareRepository;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventShareMapper eventShareMapper;

    @InjectMocks
    private EventSharingServiceImpl eventSharingService;

    private User ownerUser;
    private User sharedUser;
    private CalendarEvent testEvent;
    private EventShare testShare;
    private EventShareRequest testRequest;
    private EventShareResponse testResponse;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");
        ownerUser.setName("소유자");

        sharedUser = new User();
        sharedUser.setId(2L);
        sharedUser.setUsername("shared");
        sharedUser.setName("공유받는 사용자");

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);

        testEvent = new CalendarEvent(
            ownerUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            null
        );
        testEvent.setId(1L);

        testShare = new EventShare(testEvent, sharedUser, SharePermission.VIEW_ONLY);
        testShare.setId(1L);

        testRequest = new EventShareRequest("shared", SharePermission.VIEW_ONLY);

        testResponse = new EventShareResponse();
        testResponse.setId(1L);
        testResponse.setEventId(1L);
        testResponse.setSharedWithUsername("shared");
        testResponse.setPermission(SharePermission.VIEW_ONLY);
    }

    @Test
    @DisplayName("이벤트 공유 성공 테스트")
    void shareEventSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("shared")).thenReturn(Optional.of(sharedUser));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.empty());
        when(eventShareRepository.countByEventId(1L)).thenReturn(5L);
        when(eventShareRepository.save(any(EventShare.class))).thenReturn(testShare);
        when(eventShareMapper.toResponse(testShare)).thenReturn(testResponse);

        // when
        EventShareResponse result = eventSharingService.shareEvent("owner", 1L, testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSharedWithUsername()).isEqualTo("shared");
        verify(eventShareRepository).save(any(EventShare.class));
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 공유 시 예외 발생")
    void shareNonExistentEvent() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(999L, "owner")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventSharingService.shareEvent("owner", 999L, testRequest))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessage("이벤트를 찾을 수 없거나 소유권이 없습니다: 999");
    }

    @Test
    @DisplayName("존재하지 않는 사용자와 공유 시 예외 발생")
    void shareWithNonExistentUser() {
        // given
        EventShareRequest invalidRequest = new EventShareRequest("nonexistent", SharePermission.VIEW_ONLY);
        
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventSharingService.shareEvent("owner", 1L, invalidRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다: nonexistent");
    }

    @Test
    @DisplayName("자기 자신과 공유 시 예외 발생")
    void shareWithSelf() {
        // given
        EventShareRequest selfRequest = new EventShareRequest("owner", SharePermission.VIEW_ONLY);
        
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));

        // when & then
        assertThatThrownBy(() -> eventSharingService.shareEvent("owner", 1L, selfRequest))
            .isInstanceOf(EventSharingException.class)
            .hasMessage("자기 자신과는 이벤트를 공유할 수 없습니다");
    }

    @Test
    @DisplayName("이미 공유된 사용자와 재공유 시 예외 발생")
    void shareAlreadySharedEvent() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("shared")).thenReturn(Optional.of(sharedUser));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.of(testShare));

        // when & then
        assertThatThrownBy(() -> eventSharingService.shareEvent("owner", 1L, testRequest))
            .isInstanceOf(EventSharingException.class)
            .hasMessage("이미 해당 사용자와 공유된 이벤트입니다");
    }

    @Test
    @DisplayName("공유 개수 제한 초과 시 예외 발생")
    void shareExceedsLimit() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("shared")).thenReturn(Optional.of(sharedUser));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.empty());
        when(eventShareRepository.countByEventId(1L)).thenReturn(20L); // 최대 개수

        // when & then
        assertThatThrownBy(() -> eventSharingService.shareEvent("owner", 1L, testRequest))
            .isInstanceOf(EventSharingException.class)
            .hasMessage("이벤트당 최대 20명까지만 공유할 수 있습니다");
    }

    @Test
    @DisplayName("이벤트 공유 해제 성공 테스트")
    void unshareEventSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.of(testShare));

        // when
        eventSharingService.unshareEvent("owner", 1L, "shared");

        // then
        verify(eventShareRepository).delete(testShare);
    }

    @Test
    @DisplayName("존재하지 않는 공유 해제 시 예외 발생")
    void unshareNonExistentShare() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventSharingService.unshareEvent("owner", 1L, "shared"))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessage("해당 사용자와 공유된 이벤트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("공유 권한 변경 성공 테스트")
    void updateSharePermissionSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(Optional.of(testShare));
        when(eventShareRepository.save(testShare)).thenReturn(testShare);
        when(eventShareMapper.toResponse(testShare)).thenReturn(testResponse);

        // when
        EventShareResponse result = eventSharingService.updateSharePermission("owner", 1L, "shared", SharePermission.EDIT);

        // then
        assertThat(result).isNotNull();
        verify(testShare).changePermission(SharePermission.EDIT);
        verify(eventShareRepository).save(testShare);
    }

    @Test
    @DisplayName("이벤트 공유 목록 조회 테스트")
    void getEventSharesSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(eventShareRepository.findByEventIdOrderBySharedAtAsc(1L)).thenReturn(Arrays.asList(testShare));
        when(eventShareMapper.toResponseList(Arrays.asList(testShare))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventShareResponse> result = eventSharingService.getEventShares("owner", 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("공유받은 이벤트 목록 조회 테스트")
    void getSharedWithUserEventsSuccess() {
        // given
        when(eventShareRepository.findBySharedWithUserUsernameOrderBySharedAtDesc("shared"))
            .thenReturn(Arrays.asList(testShare));
        when(eventShareMapper.toResponseList(Arrays.asList(testShare))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventShareResponse> result = eventSharingService.getSharedWithUserEvents("shared");

        // then
        assertThat(result).hasSize(1);
        verify(eventShareRepository).findBySharedWithUserUsernameOrderBySharedAtDesc("shared");
    }

    @Test
    @DisplayName("소유 이벤트 공유 목록 조회 테스트")
    void getOwnedEventSharesSuccess() {
        // given
        when(eventShareRepository.findByEventOwnerUsername("owner")).thenReturn(Arrays.asList(testShare));
        when(eventShareMapper.toResponseList(Arrays.asList(testShare))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventShareResponse> result = eventSharingService.getOwnedEventShares("owner");

        // then
        assertThat(result).hasSize(1);
        verify(eventShareRepository).findByEventOwnerUsername("owner");
    }

    @Test
    @DisplayName("편집 가능한 공유 이벤트 조회 테스트")
    void getEditableSharedEventsSuccess() {
        // given
        when(eventShareRepository.findEditableSharedEvents("shared")).thenReturn(Arrays.asList(testShare));
        when(eventShareMapper.toResponseList(Arrays.asList(testShare))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventShareResponse> result = eventSharingService.getEditableSharedEvents("shared");

        // then
        assertThat(result).hasSize(1);
        verify(eventShareRepository).findEditableSharedEvents("shared");
    }

    @Test
    @DisplayName("이벤트 공유 여부 확인 테스트")
    void isEventSharedWithTest() {
        // given
        when(eventShareRepository.existsByEventIdAndSharedWithUserUsername(1L, "shared")).thenReturn(true);
        when(eventShareRepository.existsByEventIdAndSharedWithUserUsername(1L, "other")).thenReturn(false);

        // when & then
        assertThat(eventSharingService.isEventSharedWith(1L, "shared")).isTrue();
        assertThat(eventSharingService.isEventSharedWith(1L, "other")).isFalse();
    }

    @Test
    @DisplayName("이벤트 공유 정보 조회 테스트")
    void getEventShareTest() {
        // given
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "shared"))
            .thenReturn(Optional.of(testShare));
        when(eventShareRepository.findByEventIdAndSharedWithUserUsername(1L, "other"))
            .thenReturn(Optional.empty());
        when(eventShareMapper.toResponse(testShare)).thenReturn(testResponse);

        // when & then
        assertThat(eventSharingService.getEventShare(1L, "shared")).isNotNull();
        assertThat(eventSharingService.getEventShare(1L, "other")).isNull();
    }

    @Test
    @DisplayName("이벤트 공유 개수 조회 테스트")
    void getEventShareCountSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));
        when(eventShareRepository.countByEventId(1L)).thenReturn(5L);

        // when
        long count = eventSharingService.getEventShareCount("owner", 1L);

        // then
        assertThat(count).isEqualTo(5L);
        verify(eventShareRepository).countByEventId(1L);
    }

    @Test
    @DisplayName("공유받은 이벤트 개수 조회 테스트")
    void getSharedWithUserEventCountTest() {
        // given
        when(eventShareRepository.countBySharedWithUserUsername("shared")).thenReturn(10L);

        // when
        long count = eventSharingService.getSharedWithUserEventCount("shared");

        // then
        assertThat(count).isEqualTo(10L);
        verify(eventShareRepository).countBySharedWithUserUsername("shared");
    }

    @Test
    @DisplayName("이벤트 모든 공유 삭제 테스트")
    void deleteAllEventSharesSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "owner")).thenReturn(Optional.of(testEvent));

        // when
        eventSharingService.deleteAllEventShares("owner", 1L);

        // then
        verify(eventShareRepository).deleteByEventId(1L);
    }

    @Test
    @DisplayName("사용자 모든 공유 삭제 테스트")
    void deleteAllUserSharesSuccess() {
        // when
        eventSharingService.deleteAllUserShares("shared");

        // then
        verify(eventShareRepository).deleteBySharedWithUserUsername("shared");
    }

    @Test
    @DisplayName("소유 이벤트 모든 공유 삭제 테스트")
    void deleteAllOwnedEventSharesSuccess() {
        // when
        eventSharingService.deleteAllOwnedEventShares("owner");

        // then
        verify(eventShareRepository).deleteByEventOwnerUsername("owner");
    }
}