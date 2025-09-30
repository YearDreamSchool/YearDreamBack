package elice.yeardreamback.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "\"user\"")
public class User {

    /**
     * 기본 키 (Primary Key). 자동 증가 전략을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자의 고유 ID. 로그인 시 사용되는 식별자입니다. (예: 이메일 또는 providerId)
     */
    private String username;

    /**
     * 사용자의 실제 이름 또는 별명입니다.
     */
    private String name;

    /**
     * 사용자의 이메일 주소입니다.
     */
    private String email;

    /**
     * 사용자의 권한 등급입니다. (예: "USER", "COACH", "ADMIN")
     */
    private String role;

    /**
     * 사용자의 프로필 이미지 URL 또는 파일 경로입니다.
     */
    private String profileImg;

    /**
     * 사용자의 전화번호입니다.
     */
    private String phone;

    /**
     * 엔티티가 생성된 시각입니다. (자동 생성, 업데이트 불가능)
     */
    @CreatedBy
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 마지막으로 수정된 시각입니다. (자동 업데이트)
     */
    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}