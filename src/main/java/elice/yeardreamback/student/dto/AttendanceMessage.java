package elice.yeardreamback.student.dto;

import elice.yeardreamback.student.enums.StudentStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * WebSocket (STOMP) 메시지를 통해 클라이언트로부터
 * 학생의 출결 상태 업데이트 정보를 수신하는 DTO입니다.
 */
@Getter
@Setter
public class AttendanceMessage {

    /**
     * 업데이트 대상 학생의 고유 ID (Primary Key)입니다.
     */
    private Long studentId;

    /**
     * 업데이트 대상 학생의 좌석 번호입니다.
     */
    private Integer seatNum;

    /**
     * 학생에게 설정할 새로운 출결 상태입니다. (예: PRESENT, LATE, ABSENT)
     */
    private StudentStatus newStatus;

    /**
     * 상태가 업데이트된 시각을 나타내는 문자열입니다.
     * ISO 8601 형식의 문자열(예: "2024-12-01T14:00:00Z")로 전달되어야 합니다.
     */
    private String updatedAt;
}