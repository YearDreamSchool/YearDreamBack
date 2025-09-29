package elice.yeardreamback.student.entity;

import elice.yeardreamback.student.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "seat_num")
    private Integer seatNum;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
