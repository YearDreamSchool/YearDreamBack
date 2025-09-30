package elice.yeardreamback.student.repository;

import elice.yeardreamback.student.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Students 엔티티에 대한 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받습니다.
 */
@Repository
public interface StudentRepository extends JpaRepository<Students, Long> {

    /**
     * 좌석 번호(seatNum)를 기준으로 Students 엔티티를 조회합니다.
     * 좌석 번호는 고유하다고 가정하며, 매치되는 엔티티를 반환합니다.
     * @param seatNum 조회할 학생의 좌석 번호
     * @return 주어진 좌석 번호와 일치하는 Students 엔티티
     */
    Students findBySeatNum(int seatNum);

    /**
     * 데이터베이스에 저장된 모든 Students 엔티티 목록을 조회합니다.
     * JpaRepository에서 기본으로 제공하는 findAll() 메서드를 오버라이드하여 명시적으로 정의할 수 있습니다.
     * @return 모든 학생 정보를 담은 Students 엔티티의 리스트
     */
    List<Students> findAll();
}