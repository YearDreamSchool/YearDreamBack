package elice.yeardreamback.user.mapper;

import elice.yeardreamback.user.dto.UserDTO;
import elice.yeardreamback.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 정적 인스턴스 (필요에 따라 사용)
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * User 엔티티를 UserDTO로 변환합니다.
     * 필드 이름이 모두 동일하므로 @Mapping 어노테이션은 필요 없습니다.
     *
     * @param user 변환할 User 엔티티
     * @return UserDTO
     */
    UserDTO toDto(User user);

    /**
     * UserDTO를 User 엔티티로 변환합니다.
     * 필드 이름이 모두 동일하므로 @Mapping 어노테이션은 필요 없습니다.
     *
     * @param userDTO 변환할 UserDTO
     * @return User 엔티티
     */
    User toEntity(UserDTO userDTO);
}