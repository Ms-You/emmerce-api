package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table
public class CartProduct {

    @Id
    @Column(value = "cart_product_id")
    private Long cartProductId;

    private Long cartId;

    private Long productId;


    @Builder
    private CartProduct(Long cartId, Long productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

}
