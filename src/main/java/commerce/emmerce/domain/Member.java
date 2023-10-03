package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table("member")
public class Member {

    @Id
    @Column(value = "member_id")
    private Long memberId;

    private String name;

    private String email;

    private String password;

    private String tel;

    private String birth;

    private String profileImg;

    private Integer point;  // 보유 포인트

    private RoleType role;

    private String city;

    private String street;

    private String zipcode;


    @Builder(builderMethodName = "createMember")
    private Member(String name, String email, String password, String tel, String birth, String profileImg,
                   Integer point, RoleType role, String city, String street, String zipcode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.tel = tel;
        this.birth = birth;
        this.profileImg = profileImg;
        this.point = point;
        this.role = role;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

}
