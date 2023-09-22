package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Member {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String passwordConfirm;

    private String tel;

    private String birth;

    private String profileImg;

    private Integer point;  // 보유 포인트

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Embedded
    private Address address;    // 주소


    @Builder(builderMethodName = "createMember")
    private Member(String name, String email, String password, String passwordConfirm, String tel,
                   String birth, String profileImg, Integer point, RoleType role, Address address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.tel = tel;
        this.birth = birth;
        this.profileImg = profileImg;
        this.point = point;
        this.role = role;
        this.address = address;
    }

}
