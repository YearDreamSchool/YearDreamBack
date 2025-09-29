package elice.yeardreamback.calender.repository;

import elice.yeardreamback.calender.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * EventCategory 엔티티를 위한 리포지토리 인터페이스
 * 이벤트 카테고리 데이터 접근을 담당
 */
@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {

    /**
     * 사용자명으로 모든 카테고리 조회 (생성일 순)
     */
    List<EventCategory> findByUserUsernameOrderByCreatedAtAsc(String username);

    /**
     * 사용자명과 카테고리 ID로 특정 카테고리 조회 (소유권 확인)
     */
    Optional<EventCategory> findByIdAndUserUsername(Long id, String username);

    /**
     * 사용자명과 카테고리 이름으로 카테고리 조회 (중복 확인용)
     */
    Optional<EventCategory> findByUserUsernameAndName(String username, String name);

    /**
     * 사용자명과 카테고리 이름으로 카테고리 존재 여부 확인 (특정 ID 제외)
     */
    @Query("SELECT COUNT(c) > 0 FROM EventCategory c WHERE c.user.username = :username " +
           "AND c.name = :name AND (:categoryId IS NULL OR c.id != :categoryId)")
    boolean existsByUserUsernameAndNameExcludingId(@Param("username") String username,
                                                  @Param("name") String name,
                                                  @Param("categoryId") Long categoryId);

    /**
     * 사용자의 카테고리 개수 조회
     */
    long countByUserUsername(String username);

    /**
     * 이벤트가 있는 카테고리 조회 (삭제 불가능한 카테고리)
     */
    @Query("SELECT c FROM EventCategory c WHERE c.user.username = :username " +
           "AND SIZE(c.events) > 0 ORDER BY c.createdAt ASC")
    List<EventCategory> findCategoriesWithEvents(@Param("username") String username);

    /**
     * 이벤트가 없는 카테고리 조회 (삭제 가능한 카테고리)
     */
    @Query("SELECT c FROM EventCategory c WHERE c.user.username = :username " +
           "AND SIZE(c.events) = 0 ORDER BY c.createdAt ASC")
    List<EventCategory> findCategoriesWithoutEvents(@Param("username") String username);

    /**
     * 특정 색상을 사용하는 카테고리 개수 조회
     */
    long countByUserUsernameAndColor(String username, String color);
}