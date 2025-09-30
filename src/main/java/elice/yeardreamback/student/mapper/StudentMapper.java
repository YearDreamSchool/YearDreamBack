package elice.yeardreamback.student.mapper;

import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.entity.Students;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);

    /**
     * Students 엔티티를 StudentReseponse DTO로 변환합니다.
     * 엔티티 필드 이름(getSeatNum)과 DTO 필드 이름(seat)이 다르므로 @Mapping을 사용하여 명시적으로 연결합니다.
     * 나머지 필즈 (id, name, status, updatedAt)는 이름이 같으므로 MapStruct가 자동으로 매핑합니다.
     *
     * @param student 변환할 Students 엔티티
     * @return StudentsResponse DTO
     */
    @Mapping(source = "seatNum", target = "seat")
    StudentResponse toResponseDto(Students student);
}
