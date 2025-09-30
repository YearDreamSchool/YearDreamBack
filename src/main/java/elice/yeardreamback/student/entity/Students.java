package elice.yeardreamback.student.entity;

import elice.yeardreamback.student.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

/**
 * 강의실 내 학생 정보를 나타내는 엔티티 클래스입니다.
 * 학생의 좌석 정보와 현재 출결 상태를 관리합니다.
 */
@Entity
@Getter
@Setter
public class Students {

    /**
     * 학생 엔티티의 기본 키 (Primary Key). 자동 증가 전략을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 학생의 이름입니다.
     */
    private String name;

    /**
     * 학생이 배정된 강의실 내 좌석 번호입니다.
     * 이 값은 고유 식별자로 사용될 수 있습니다.
     */
    @Column(name = "seat_num")
    private Integer seatNum;

    /**
     * 학생의 현재 출결 상태입니다. (예: PRESENT, LATE, ABSENT)
     * 데이터베이스에는 Enum의 문자열 값으로 저장됩니다.
     */
    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    /**
     * 출결 상태가 마지막으로 업데이트된 시각입니다.
     */
    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}