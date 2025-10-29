package elice.yeardreamback.user.entity;

import elice.yeardreamback.user.enums.UserRoleType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * 사용자의 실제 이름 또는 별명입니다.
     */
    @Column(name = "name")
    private String name;

    /**
     * 사용자의 이메일 주소입니다.
     */
    @Column(name = "email")
    private String email;

    /**
     * 사용자의 권한 등급입니다. (예: "USER", "COACH", "ADMIN")
     */
    @Column(name = "role", nullable = false)
    private String role = UserRoleType.ROLE_USER.toString();

    /**
     * 사용자의 프로필 이미지 URL 또는 파일 경로입니다.
     */
    @Column(name = "profile_img")
    private String profileImg;

    /**
     * 사용자의 전화번호입니다.
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 엔티티가 생성된 시각입니다. (자동 생성, 업데이트 불가능)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 마지막으로 수정된 시각입니다. (자동 업데이트)
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}