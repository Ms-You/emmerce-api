package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String email;

    private String password;

    private String password_confirm;

    private String tel;

    private String birth;

    private String profile_img;

    private Integer point;  // 보유 포인트

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Embedded
    private Address address;    // 주소


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;  // 장바구니

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Heart> heart_list = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Review> review_list = new ArrayList<>();

}
