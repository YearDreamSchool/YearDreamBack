package elice.yeardreamback.user.dto;

public record LoginUserResponse(
        /**
         * 사용자의 고유 식별자 (ID) 또는 로그인에 사용되는 값입니다.
         * OAuth2 로그인 시, 이메일이거나 'provider_id' 형식일 수 있습니다.
         */
        String username,
        /**
         * 사용자에게 표시되는 이름 또는 닉네임입니다. (예: 홍길동, Elice 학생)
         */
        String name,
        /**
         * 사용자의 권한 (Role) 정보입니다.
         * 일반적으로 'USER', 'COACH', 'ADMIN' 등 권한 코드를 가집니다. (예: "ADMIN")
         */
        String role
) {}