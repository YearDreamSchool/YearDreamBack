package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.exception.AccessDeniedException;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.exception.CategoryNotFoundException;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.repository.EventShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 접근 제어 서비스
 * 이벤트 및 카테고리에 대한 권한 검증을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessControlService {

    private final CalendarEventRepository calendarEventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventShareRepository eventShareRepository;

    /**
     * 이벤트 읽기 권한 검증
     * 소유자이거나 공유받은 사용자인지 확인합니다.
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 검증된 이벤트 엔티티
     * @throws EventNotFoundException 이벤트를 찾을 수 없는 경우
     * @throws AccessDeniedException 접근 권한이 없는 경우
     */
    public CalendarEvent verifyEventReadAccess(String username, Long eventId) {
        log.debug("이벤트 읽기 권한 검증: 사용자={}, 이벤트ID={}", username, eventId);
        
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다: " + eventId));
        
        // 소유자인지 확인
        if (event.getOwnerUsername().equals(username)) {
            log.debug("이벤트 소유자 접근 허용: 사용자={}, 이벤트ID={}", username, eventId);
            return event;
        }
        
        // 공유받은 사용자인지 확인
        Optional<EventShare> eventShare = eventShareRepository
                .findByEventIdAndSharedWithUserUsername(eventId, username);
        
        if (eventShare.isPresent()) {
            log.debug("공유된 이벤트 접근 허용: 사용자={}, 이벤트ID={}, 권한={}", 
                    username, eventId, eventShare.get().getPermission());
            return event;
        }
        
        log.warn("이벤트 접근 권한 없음: 사용자={}, 이벤트ID={}", username, eventId);
        throw new AccessDeniedException("이벤트에 대한 접근 권한이 없습니다: " + eventId);
    }

    /**
     * 이벤트 편집 권한 검증
     * 소유자이거나 편집 권한이 있는 공유 사용자인지 확인합니다.
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 검증된 이벤트 엔티티
     * @throws EventNotFoundException 이벤트를 찾을 수 없는 경우
     * @throws AccessDeniedException 편집 권한이 없는 경우
     */
    public CalendarEvent verifyEventEditAccess(String username, Long eventId) {
        log.debug("이벤트 편집 권한 검증: 사용자={}, 이벤트ID={}", username, eventId);
        
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다: " + eventId));
        
        // 소유자인지 확인
        if (event.getOwnerUsername().equals(username)) {
            log.debug("이벤트 소유자 편집 허용: 사용자={}, 이벤트ID={}", username, eventId);
            return event;
        }
        
        // 편집 권한이 있는 공유 사용자인지 확인
        Optional<EventShare> eventShare = eventShareRepository
                .findByEventIdAndSharedWithUserUsername(eventId, username);
        
        if (eventShare.isPresent() && eventShare.get().getPermission() == SharePermission.EDIT) {
            log.debug("공유된 이벤트 편집 허용: 사용자={}, 이벤트ID={}", username, eventId);
            return event;
        }
        
        log.warn("이벤트 편집 권한 없음: 사용자={}, 이벤트ID={}", username, eventId);
        throw new AccessDeniedException("이벤트에 대한 편집 권한이 없습니다: " + eventId);
    }

    /**
     * 이벤트 삭제 권한 검증
     * 소유자만 삭제할 수 있습니다.
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 검증된 이벤트 엔티티
     * @throws EventNotFoundException 이벤트를 찾을 수 없는 경우
     * @throws AccessDeniedException 삭제 권한이 없는 경우
     */
    public CalendarEvent verifyEventDeleteAccess(String username, Long eventId) {
        log.debug("이벤트 삭제 권한 검증: 사용자={}, 이벤트ID={}", username, eventId);
        
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다: " + eventId));
        
        // 소유자만 삭제 가능
        if (!event.getOwnerUsername().equals(username)) {
            log.warn("이벤트 삭제 권한 없음: 사용자={}, 이벤트ID={}, 소유자={}", 
                    username, eventId, event.getOwnerUsername());
            throw new AccessDeniedException("이벤트 삭제는 소유자만 가능합니다: " + eventId);
        }
        
        log.debug("이벤트 삭제 권한 확인: 사용자={}, 이벤트ID={}", username, eventId);
        return event;
    }

    /**
     * 카테고리 접근 권한 검증
     * 소유자만 접근할 수 있습니다.
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     * @return 검증된 카테고리 엔티티
     * @throws CategoryNotFoundException 카테고리를 찾을 수 없는 경우
     * @throws AccessDeniedException 접근 권한이 없는 경우
     */
    public EventCategory verifyCategoryAccess(String username, Long categoryId) {
        log.debug("카테고리 접근 권한 검증: 사용자={}, 카테고리ID={}", username, categoryId);
        
        EventCategory category = eventCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
        
        if (!category.getOwnerUsername().equals(username)) {
            log.warn("카테고리 접근 권한 없음: 사용자={}, 카테고리ID={}, 소유자={}", 
                    username, categoryId, category.getOwnerUsername());
            throw new AccessDeniedException("카테고리에 대한 접근 권한이 없습니다: " + categoryId);
        }
        
        log.debug("카테고리 접근 권한 확인: 사용자={}, 카테고리ID={}", username, categoryId);
        return category;
    }

    /**
     * 이벤트 공유 권한 검증
     * 이벤트 소유자만 공유할 수 있습니다.
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 검증된 이벤트 엔티티
     * @throws EventNotFoundException 이벤트를 찾을 수 없는 경우
     * @throws AccessDeniedException 공유 권한이 없는 경우
     */
    public CalendarEvent verifyEventShareAccess(String username, Long eventId) {
        log.debug("이벤트 공유 권한 검증: 사용자={}, 이벤트ID={}", username, eventId);
        
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다: " + eventId));
        
        if (!event.getOwnerUsername().equals(username)) {
            log.warn("이벤트 공유 권한 없음: 사용자={}, 이벤트ID={}, 소유자={}", 
                    username, eventId, event.getOwnerUsername());
            throw new AccessDeniedException("이벤트 공유는 소유자만 가능합니다: " + eventId);
        }
        
        log.debug("이벤트 공유 권한 확인: 사용자={}, 이벤트ID={}", username, eventId);
        return event;
    }

    /**
     * 사용자가 이벤트에 대한 편집 권한이 있는지 확인
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 편집 권한 여부
     */
    public boolean hasEventEditPermission(String username, Long eventId) {
        try {
            verifyEventEditAccess(username, eventId);
            return true;
        } catch (EventNotFoundException | AccessDeniedException e) {
            return false;
        }
    }

    /**
     * 사용자가 이벤트에 대한 읽기 권한이 있는지 확인
     * 
     * @param username 사용자명
     * @param eventId 이벤트 ID
     * @return 읽기 권한 여부
     */
    public boolean hasEventReadPermission(String username, Long eventId) {
        try {
            verifyEventReadAccess(username, eventId);
            return true;
        } catch (EventNotFoundException | AccessDeniedException e) {
            return false;
        }
    }

    /**
     * 사용자가 카테고리에 대한 접근 권한이 있는지 확인
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     * @return 접근 권한 여부
     */
    public boolean hasCategoryAccess(String username, Long categoryId) {
        try {
            verifyCategoryAccess(username, categoryId);
            return true;
        } catch (CategoryNotFoundException | AccessDeniedException e) {
            return false;
        }
    }
}