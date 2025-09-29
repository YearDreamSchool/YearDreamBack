package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventShareRequest;
import elice.yeardreamback.calender.dto.EventShareResponse;
import elice.yeardreamback.calender.enums.SharePermission;

import java.util.List;

/**
 * 이벤트 공유 서비스 인터페이스
 * 이벤트 공유 및 권한 관리 비즈니스 로직을 담당
 */
public interface EventSharingService {

    /**
     * 이벤트를 다른 사용자와 공유
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     * @param request 공유 요청
     * @return 생성된 공유 응답
     */
    EventShareResponse shareEvent(String ownerUsername, Long eventId, EventShareRequest request);

    /**
     * 이벤트 공유 해제
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     * @param sharedWithUsername 공유 해제할 사용자명
     */
    void unshareEvent(String ownerUsername, Long eventId, String sharedWithUsername);

    /**
     * 이벤트 공유 권한 변경
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     * @param sharedWithUsername 권한을 변경할 사용자명
     * @param newPermission 새로운 권한
     * @return 수정된 공유 응답
     */
    EventShareResponse updateSharePermission(String ownerUsername, Long eventId, 
                                           String sharedWithUsername, SharePermission newPermission);

    /**
     * 특정 이벤트의 모든 공유 조회
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     * @return 공유 목록
     */
    List<EventShareResponse> getEventShares(String ownerUsername, Long eventId);

    /**
     * 특정 사용자와 공유된 모든 이벤트 조회
     * 
     * @param username 공유받은 사용자명
     * @return 공유받은 이벤트 목록
     */
    List<EventShareResponse> getSharedWithUserEvents(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 모든 공유 조회
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @return 소유한 이벤트의 공유 목록
     */
    List<EventShareResponse> getOwnedEventShares(String ownerUsername);

    /**
     * 편집 권한으로 공유된 이벤트 조회
     * 
     * @param username 사용자명
     * @return 편집 가능한 공유 이벤트 목록
     */
    List<EventShareResponse> getEditableSharedEvents(String username);

    /**
     * 특정 이벤트가 특정 사용자와 공유되어 있는지 확인
     * 
     * @param eventId 이벤트 ID
     * @param username 사용자명
     * @return 공유 여부
     */
    boolean isEventSharedWith(Long eventId, String username);

    /**
     * 특정 이벤트와 사용자 간의 공유 정보 조회
     * 
     * @param eventId 이벤트 ID
     * @param username 사용자명
     * @return 공유 정보 (없으면 null)
     */
    EventShareResponse getEventShare(Long eventId, String username);

    /**
     * 특정 이벤트의 공유 개수 조회
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     * @return 공유 개수
     */
    long getEventShareCount(String ownerUsername, Long eventId);

    /**
     * 특정 사용자와 공유된 이벤트 개수 조회
     * 
     * @param username 사용자명
     * @return 공유받은 이벤트 개수
     */
    long getSharedWithUserEventCount(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 총 공유 개수 조회
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @return 총 공유 개수
     */
    long getOwnedEventShareCount(String ownerUsername);

    /**
     * 특정 이벤트의 모든 공유 삭제 (이벤트 삭제 시 사용)
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     * @param eventId 이벤트 ID
     */
    void deleteAllEventShares(String ownerUsername, Long eventId);

    /**
     * 특정 사용자와의 모든 공유 삭제 (사용자 탈퇴 시 사용)
     * 
     * @param username 사용자명
     */
    void deleteAllUserShares(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 모든 공유 삭제 (사용자 탈퇴 시 사용)
     * 
     * @param ownerUsername 이벤트 소유자 사용자명
     */
    void deleteAllOwnedEventShares(String ownerUsername);
}