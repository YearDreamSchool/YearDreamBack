package elice.yeardreamback.student.repository;

import elice.yeardreamback.student.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Students, Long> {

    Students findByName(String name);
    Students findBySeatNum(int seatNum);
    List<Students> findAll();
}
