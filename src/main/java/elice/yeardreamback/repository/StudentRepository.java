package elice.yeardreamback.repository;

import elice.yeardreamback.dto.StudentResponse;
import elice.yeardreamback.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Students, Long> {

    Students findByName(String name);
    Students findBySeatNum(int seatNum);
    List<Students> findAll();
}
