package elice.yeardreamback.dto;

import elice.yeardreamback.entity.Students;
import elice.yeardreamback.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String name;
    private Integer seat;
    private StudentStatus status;
    private LocalDateTime updatedAt;

    public static StudentResponse fromEntity(Students student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getSeatNum(),
                student.getStatus(),
                student.getUpdatedAt()
        );
    }
}
