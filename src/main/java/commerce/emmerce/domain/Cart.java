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
public class Cart {

    @Id
    @GeneratedValue
    @Column(name = "cart_id")
    private Long id;


    @OneToMany(mappedBy = "cart", orphanRemoval = true)
    private List<CartProduct> cartProductList = new ArrayList<>();


    @Builder(builderMethodName = "createCart")
    private Cart(Member member) {

        member.insertCart(this);
    }

}
