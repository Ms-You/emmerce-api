package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberReq {

    private String name;

    private String email;

    private String password;

    private String passwordConfirm;

    private String tel;

    private String birth;

}
