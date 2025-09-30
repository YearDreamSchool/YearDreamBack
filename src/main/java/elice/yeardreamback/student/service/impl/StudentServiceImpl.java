package elice.yeardreamback.student.service.impl;

import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.entity.Students;
import elice.yeardreamback.student.enums.StudentStatus;
import elice.yeardreamback.student.mapper.StudentMapper;
import elice.yeardreamback.student.repository.StudentRepository;
import elice.yeardreamback.student.service.StudentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param studentRepository 학생 엔티티 데이터 접근 리포지토리
     * @param studentMapper Students 엔티티를 StudentResponse DTO로 변환하는 매퍼
     */
    public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    /**
     * 데이터베이스에 저장된 전체 학생 목록을 조회하고, 이를 DTO로 변환하여 반환합니다.
     * @return 모든 학생의 정보를 담은 StudentResponse 리스트
     */
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(response -> new StudentResponse(response.getId(), response.getName(), response.getSeatNum(), response.getStatus(), response.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 좌석 번호의 학생 출결 상태를 업데이트합니다.
     * * @param seatNum 상태를 업데이트할 학생의 좌석 번호
     * @param newStatus 새로 설정할 출결 상태
     * @param updatedAt 상태 업데이트 시각
     * @return 업데이트된 학생 정보를 담은 StudentResponse DTO
     * @throws IllegalArgumentException 해당 좌석 번호의 학생을 찾을 수 없을 경우
     */
    @Override
    public StudentResponse updateStatus(int seatNum, StudentStatus newStatus, LocalDateTime updatedAt) {
        Students student = studentRepository.findBySeatNum(seatNum);
        if (student == null) {
            throw new IllegalArgumentException("Student with seat number " + seatNum + " not found.");
        }
        student.setStatus(StudentStatus.valueOf(newStatus.name()));
        student.setUpdatedAt(updatedAt);
        studentRepository.save(student);
        return studentMapper.toResponseDto(student);
    }

}
