package elice.yeardreamback.student.dto;

import elice.yeardreamback.student.entity.Students; // (주석 처리) 매퍼로 분리되었으므로 이 임포트는 필요 없음
import elice.yeardreamback.student.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 학생 목록 조회 또는 실시간 출결 업데이트 후 클라이언트에게 반환되는 응답 DTO입니다.
 * 학생의 현재 상태 정보를 포함합니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class StudentResponse {

    /**
     * 학생의 고유 식별자 (Primary Key).
     */
    private Long id;

    /**
     * 학생의 이름입니다.
     */
    private String name;

    /**
     * 강의실 내 학생의 좌석 번호입니다.
     */
    private Integer seat;

    /**
     * 학생의 현재 출결 상태입니다. (예: PRESENT, LATE, ABSENT)
     */
    private StudentStatus status;

    /**
     * 해당 학생의 출결 상태가 마지막으로 업데이트된 시각입니다.
     */
    private LocalDateTime updatedAt;
}