package elice.yeardreamback.controller;

import elice.yeardreamback.dto.StudentResponse;
import elice.yeardreamback.entity.Students;
import elice.yeardreamback.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> findAll() {
        return studentService.findAll();
    }
}
