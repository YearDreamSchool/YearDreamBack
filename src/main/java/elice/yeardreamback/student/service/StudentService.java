package elice.yeardreamback.student.service;

import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.enums.StudentStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface StudentService {

    List<StudentResponse> findAll();
    StudentResponse updateStatus(int seatNum, StudentStatus newStatus, LocalDateTime updatedAt);
}
