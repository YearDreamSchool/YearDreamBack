package elice.yeardreamback.calender.repository;

import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.SharePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * EventShare 엔티티를 위한 리포지토리 인터페이스
 * 이벤트 공유 데이터 접근을 담당
 */
@Repository
public interface EventShareRepository extends JpaRepository<EventShare, Long> {

    /**
     * 특정 이벤트의 모든 공유 조회
     */
    List<EventShare> findByEventIdOrderBySharedAtAsc(Long eventId);

    /**
     * 특정 이벤트와 사용자 간의 공유 조회
     */
    Optional<EventShare> findByEventIdAndSharedWithUserUsername(Long eventId, String username);

    /**
     * 특정 사용자와 공유된 모든 이벤트 공유 조회
     */
    List<EventShare> findBySharedWithUserUsernameOrderBySharedAtDesc(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 모든 공유 조회
     */
    @Query("SELECT s FROM EventShare s WHERE s.event.user.username = :ownerUsername " +
           "ORDER BY s.sharedAt DESC")
    List<EventShare> findByEventOwnerUsername(@Param("ownerUsername") String ownerUsername);

    /**
     * 특정 권한으로 공유된 이벤트 조회
     */
    List<EventShare> findBySharedWithUserUsernameAndPermissionOrderBySharedAtDesc(
            String username, SharePermission permission);

    /**
     * 편집 권한으로 공유된 이벤트 조회
     */
    @Query("SELECT s FROM EventShare s WHERE s.sharedWithUser.username = :username " +
           "AND s.permission = 'EDIT' ORDER BY s.sharedAt DESC")
    List<EventShare> findEditableSharedEvents(@Param("username") String username);

    /**
     * 특정 이벤트가 특정 사용자와 공유되어 있는지 확인
     */
    boolean existsByEventIdAndSharedWithUserUsername(Long eventId, String username);

    /**
     * 특정 이벤트의 공유 개수 조회
     */
    long countByEventId(Long eventId);

    /**
     * 특정 사용자와 공유된 이벤트 개수 조회
     */
    long countBySharedWithUserUsername(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 총 공유 개수 조회
     */
    @Query("SELECT COUNT(s) FROM EventShare s WHERE s.event.user.username = :ownerUsername")
    long countByEventOwnerUsername(@Param("ownerUsername") String ownerUsername);

    /**
     * 특정 이벤트와 사용자 간의 공유 삭제
     */
    void deleteByEventIdAndSharedWithUserUsername(Long eventId, String username);

    /**
     * 특정 이벤트의 모든 공유 삭제
     */
    void deleteByEventId(Long eventId);

    /**
     * 특정 사용자와의 모든 공유 삭제 (사용자 탈퇴 시)
     */
    void deleteBySharedWithUserUsername(String username);

    /**
     * 특정 사용자가 소유한 이벤트의 모든 공유 삭제
     */
    @Query("DELETE FROM EventShare s WHERE s.event.user.username = :ownerUsername")
    void deleteByEventOwnerUsername(@Param("ownerUsername") String ownerUsername);
}