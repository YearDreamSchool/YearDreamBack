package elice.yeardreamback.student.service;

import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.enums.StudentStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학생(Student) 및 출결 상태 관리와 관련된 비즈니스 로직을 처리하는 서비스 계층 인터페이스입니다.
 * 학생 목록 조회 및 실시간 출결 상태 업데이트 기능을 정의합니다.
 */
@Service
public interface StudentService {

    /**
     * 데이터베이스에 저장된 모든 학생의 정보를 조회합니다.
     * 엔티티를 StudentResponse DTO로 변환하여 반환합니다.
     * @return 모든 학생 정보를 담은 StudentResponse DTO 리스트
     */
    List<StudentResponse> findAll();

    /**
     * 특정 좌석 번호의 학생 출결 상태를 업데이트합니다.
     * 이 메서드는 WebSocket 메시지를 통해 호출되며, 데이터베이스에 변경 사항을 반영합니다.
     * @param seatNum 상태를 업데이트할 학생의 좌석 번호
     * @param newStatus 새로 설정할 출결 상태 (예: PRESENT, LATE, ABSENT)
     * @param updatedAt 상태 업데이트가 발생한 시각
     * @return 업데이트된 학생 정보를 담은 StudentResponse DTO
     */
    StudentResponse updateStatus(int seatNum, StudentStatus newStatus, LocalDateTime updatedAt);
}