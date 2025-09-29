package elice.yeardreamback.service.impl;

import elice.yeardreamback.dto.StudentResponse;
import elice.yeardreamback.entity.Students;
import elice.yeardreamback.enums.StudentStatus;
import elice.yeardreamback.repository.StudentRepository;
import elice.yeardreamback.service.StudentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // 전체 학생 조회
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(response -> new StudentResponse(response.getId(), response.getName(), response.getSeatNum(), response.getStatus(), response.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public StudentResponse updateStatus(int seatNum, StudentStatus newStatus, LocalDateTime updatedAt) {
        Students student = studentRepository.findBySeatNum(seatNum);
        if (student == null) {
            throw new IllegalArgumentException("Student with seat number " + seatNum + " not found.");
        }
        student.setStatus(StudentStatus.valueOf(newStatus.name()));
        student.setUpdatedAt(updatedAt);
        studentRepository.save(student);
        return StudentResponse.fromEntity(student);
    }

}
