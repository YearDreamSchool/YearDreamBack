package elice.yeardreamback.dto;

import elice.yeardreamback.entity.Students;
import elice.yeardreamback.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String name;
    private int seatNum;
    private StudentStatus status;

    public static StudentResponse fromEntity(Students student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getSeatNum(),
                student.getStatus()
        );
    }
}
