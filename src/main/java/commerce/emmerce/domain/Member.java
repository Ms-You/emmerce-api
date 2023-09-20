package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;  // 장바구니

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Heart> heartList = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();


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

    public void insertCart(Cart cart) {
        this.cart = cart;
    }

}
