package elice.yeardreamback.dto;

import elice.yeardreamback.enums.StudentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceMessage {

    private Long studentId;
    private Integer seatNum;
    private StudentStatus newStatus;
    private String updatedAt;
}
