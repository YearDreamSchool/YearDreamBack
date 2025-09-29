package elice.yeardreamback.controller;

import elice.yeardreamback.dto.AttendanceMessage;
import elice.yeardreamback.dto.StudentResponse;
import elice.yeardreamback.service.StudentService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @MessageMapping("/attendance.update")
    @SendTo("/topic/attendance")
    public List<StudentResponse> updateAttendance(AttendanceMessage message) {
        // UTC 문자열 -> LocalDateTime 변환
        LocalDateTime updatedAt = LocalDateTime.ofInstant(
                Instant.parse(message.getUpdatedAt()),
                ZoneId.systemDefault()
        );

        studentService.updateStatus(message.getSeatNum(), message.getNewStatus(), updatedAt);
        return studentService.findAll();
    }
}
