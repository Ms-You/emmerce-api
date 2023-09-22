package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Cart {

    @Id
    @Column(name = "cart_id")
    private Long id;


//    @OneToMany(mappedBy = "cart", orphanRemoval = true)
    private List<CartProduct> cartProductList = new ArrayList<>();


    @Builder(builderMethodName = "createCart")
    private Cart(Member member) {

        member.insertCart(this);
    }

}
