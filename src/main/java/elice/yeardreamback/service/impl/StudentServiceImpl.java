package elice.yeardreamback.service.impl;

import elice.yeardreamback.dto.StudentResponse;
import elice.yeardreamback.repository.StudentRepository;
import elice.yeardreamback.service.StudentService;
import org.springframework.stereotype.Service;

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
                .map(response -> new StudentResponse(response.getId(), response.getName(), response.getSeatNum(), response.getStatus()))
                .collect(Collectors.toList());
    }
}
