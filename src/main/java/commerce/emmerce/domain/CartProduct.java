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
public class CartProduct {

    @Id
    @Column(name = "cart_product_id")
    private Long id;

    private Long cartId;

    private Long productId;


    @Builder
    private CartProduct(Long cartId, Long productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

}
