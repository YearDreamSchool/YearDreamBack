package elice.yeardreamback.calender.service.impl;

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
import elice.yeardreamback.calender.service.EventSharingService;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.UserNotFoundException;
import elice.yeardreamback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * EventSharingService 구현체
 * 이벤트 공유 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSharingServiceImpl implements EventSharingService {

    private final EventShareRepository eventShareRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    private final EventShareMapper eventShareMapper;

    private static final int MAX_SHARES_PER_EVENT = 20; // 이벤트당 최대 공유 수

    @Override
    @Transactional
    public EventShareResponse shareEvent(String ownerUsername, Long eventId, EventShareRequest request) {
        log.info("이벤트 공유 요청: 소유자={}, 이벤트ID={}, 공유대상={}", 
                ownerUsername, eventId, request.getSharedWithUsername());

        // 이벤트 조회 및 소유권 확인
        CalendarEvent event = findEventByIdAndOwner(eventId, ownerUsername);

        // 공유받을 사용자 조회
        User sharedWithUser = findUserByUsername(request.getSharedWithUsername());

        // 자기 자신과 공유 방지
        if (ownerUsername.equals(request.getSharedWithUsername())) {
            throw new EventSharingException("자기 자신과는 이벤트를 공유할 수 없습니다");
        }

        // 이미 공유되어 있는지 확인
        Optional<EventShare> existingShare = eventShareRepository
            .findByEventIdAndSharedWithUserUsername(eventId, request.getSharedWithUsername());
        
        if (existingShare.isPresent()) {
            throw new EventSharingException("이미 해당 사용자와 공유된 이벤트입니다");
        }

        // 공유 개수 제한 확인
        long currentShareCount = eventShareRepository.countByEventId(eventId);
        if (currentShareCount >= MAX_SHARES_PER_EVENT) {
            throw new EventSharingException("이벤트당 최대 " + MAX_SHARES_PER_EVENT + "명까지만 공유할 수 있습니다");
        }

        // 이벤트 공유 생성
        EventShare eventShare = new EventShare(event, sharedWithUser, request.getPermission());
        EventShare savedShare = eventShareRepository.save(eventShare);

        log.info("이벤트 공유 완료: ID={}, 소유자={}, 공유대상={}, 권한={}", 
                savedShare.getId(), ownerUsername, request.getSharedWithUsername(), request.getPermission());
        
        return eventShareMapper.toResponse(savedShare);
    }

    @Override
    @Transactional
    public void unshareEvent(String ownerUsername, Long eventId, String sharedWithUsername) {
        log.info("이벤트 공유 해제 요청: 소유자={}, 이벤트ID={}, 공유해제대상={}", 
                ownerUsername, eventId, sharedWithUsername);

        // 이벤트 조회 및 소유권 확인
        findEventByIdAndOwner(eventId, ownerUsername);

        // 공유 정보 조회
        EventShare eventShare = eventShareRepository
            .findByEventIdAndSharedWithUserUsername(eventId, sharedWithUsername)
            .orElseThrow(() -> new EventNotFoundException("해당 사용자와 공유된 이벤트를 찾을 수 없습니다"));

        // 공유 삭제
        eventShareRepository.delete(eventShare);

        log.info("이벤트 공유 해제 완료: 소유자={}, 이벤트ID={}, 공유해제대상={}", 
                ownerUsername, eventId, sharedWithUsername);
    }

    @Override
    @Transactional
    public EventShareResponse updateSharePermission(String ownerUsername, Long eventId, 
                                                   String sharedWithUsername, SharePermission newPermission) {
        log.info("이벤트 공유 권한 변경: 소유자={}, 이벤트ID={}, 대상={}, 새권한={}", 
                ownerUsername, eventId, sharedWithUsername, newPermission);

        // 이벤트 조회 및 소유권 확인
        findEventByIdAndOwner(eventId, ownerUsername);

        // 공유 정보 조회
        EventShare eventShare = eventShareRepository
            .findByEventIdAndSharedWithUserUsername(eventId, sharedWithUsername)
            .orElseThrow(() -> new EventNotFoundException("해당 사용자와 공유된 이벤트를 찾을 수 없습니다"));

        // 권한 변경
        eventShare.changePermission(newPermission);
        EventShare updatedShare = eventShareRepository.save(eventShare);

        log.info("이벤트 공유 권한 변경 완료: ID={}, 새권한={}", updatedShare.getId(), newPermission);
        return eventShareMapper.toResponse(updatedShare);
    }

    @Override
    public List<EventShareResponse> getEventShares(String ownerUsername, Long eventId) {
        log.debug("이벤트 공유 목록 조회: 소유자={}, 이벤트ID={}", ownerUsername, eventId);

        // 이벤트 조회 및 소유권 확인
        findEventByIdAndOwner(eventId, ownerUsername);

        List<EventShare> shares = eventShareRepository.findByEventIdOrderBySharedAtAsc(eventId);
        return eventShareMapper.toResponseList(shares);
    }

    @Override
    public List<EventShareResponse> getSharedWithUserEvents(String username) {
        log.debug("공유받은 이벤트 목록 조회: 사용자={}", username);

        List<EventShare> shares = eventShareRepository.findBySharedWithUserUsernameOrderBySharedAtDesc(username);
        return eventShareMapper.toResponseList(shares);
    }

    @Override
    public List<EventShareResponse> getOwnedEventShares(String ownerUsername) {
        log.debug("소유 이벤트 공유 목록 조회: 소유자={}", ownerUsername);

        List<EventShare> shares = eventShareRepository.findByEventOwnerUsername(ownerUsername);
        return eventShareMapper.toResponseList(shares);
    }

    @Override
    public List<EventShareResponse> getEditableSharedEvents(String username) {
        log.debug("편집 가능한 공유 이벤트 조회: 사용자={}", username);

        List<EventShare> shares = eventShareRepository.findEditableSharedEvents(username);
        return eventShareMapper.toResponseList(shares);
    }

    @Override
    public boolean isEventSharedWith(Long eventId, String username) {
        return eventShareRepository.existsByEventIdAndSharedWithUserUsername(eventId, username);
    }

    @Override
    public EventShareResponse getEventShare(Long eventId, String username) {
        log.debug("이벤트 공유 정보 조회: 이벤트ID={}, 사용자={}", eventId, username);

        Optional<EventShare> share = eventShareRepository
            .findByEventIdAndSharedWithUserUsername(eventId, username);
        
        return share.map(eventShareMapper::toResponse).orElse(null);
    }

    @Override
    public long getEventShareCount(String ownerUsername, Long eventId) {
        log.debug("이벤트 공유 개수 조회: 소유자={}, 이벤트ID={}", ownerUsername, eventId);

        // 이벤트 조회 및 소유권 확인
        findEventByIdAndOwner(eventId, ownerUsername);

        return eventShareRepository.countByEventId(eventId);
    }

    @Override
    public long getSharedWithUserEventCount(String username) {
        log.debug("공유받은 이벤트 개수 조회: 사용자={}", username);

        return eventShareRepository.countBySharedWithUserUsername(username);
    }

    @Override
    public long getOwnedEventShareCount(String ownerUsername) {
        log.debug("소유 이벤트 총 공유 개수 조회: 소유자={}", ownerUsername);

        return eventShareRepository.countByEventOwnerUsername(ownerUsername);
    }

    @Override
    @Transactional
    public void deleteAllEventShares(String ownerUsername, Long eventId) {
        log.info("이벤트 모든 공유 삭제: 소유자={}, 이벤트ID={}", ownerUsername, eventId);

        // 이벤트 조회 및 소유권 확인
        findEventByIdAndOwner(eventId, ownerUsername);

        eventShareRepository.deleteByEventId(eventId);
        log.info("이벤트 모든 공유 삭제 완료: 이벤트ID={}", eventId);
    }

    @Override
    @Transactional
    public void deleteAllUserShares(String username) {
        log.info("사용자 모든 공유 삭제: 사용자={}", username);

        eventShareRepository.deleteBySharedWithUserUsername(username);
        log.info("사용자 모든 공유 삭제 완료: 사용자={}", username);
    }

    @Override
    @Transactional
    public void deleteAllOwnedEventShares(String ownerUsername) {
        log.info("소유 이벤트 모든 공유 삭제: 소유자={}", ownerUsername);

        eventShareRepository.deleteByEventOwnerUsername(ownerUsername);
        log.info("소유 이벤트 모든 공유 삭제 완료: 소유자={}", ownerUsername);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 이벤트 ID와 소유자로 이벤트 조회
     */
    private CalendarEvent findEventByIdAndOwner(Long eventId, String ownerUsername) {
        return calendarEventRepository.findByIdAndUserUsername(eventId, ownerUsername)
            .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없거나 소유권이 없습니다: " + eventId));
    }
}