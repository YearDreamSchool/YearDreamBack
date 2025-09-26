package elice.yeardreamback.service;

import elice.yeardreamback.dto.StudentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StudentService {

    List<StudentResponse> findAll();
}
