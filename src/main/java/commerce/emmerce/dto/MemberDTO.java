package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberDTO {

    @Getter
    @NoArgsConstructor
    public static class MemberRegisterReq {
        private String name;

        private String email;

        private String password;

        private String passwordConfirm;

        private String tel;

        private String birth;
    }


    @Getter
    @NoArgsConstructor
    public static class MemberLoginReq {
        private String loginId;

        private String password;
    }

}
