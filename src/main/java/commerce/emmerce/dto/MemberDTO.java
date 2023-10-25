package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberDTO {

    @Getter
    @NoArgsConstructor
    public static class RegisterReq {
        private String name;
        private String email;
        private String password;
        private String passwordConfirm;
        private String tel;
        private String birth;
    }


    @Getter
    @NoArgsConstructor
    public static class DuplicateCheckReq {
        private String name;
    }


    @Getter
    @NoArgsConstructor
    public static class LoginReq {
        private String name;
        private String password;
    }

}
