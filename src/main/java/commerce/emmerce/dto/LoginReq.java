package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginReq {

    private String loginId;
    private String password;
}
