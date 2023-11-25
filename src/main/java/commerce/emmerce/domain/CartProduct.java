package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class CartProduct {

    @Id
    @Column(value = "cart_product_id")
    private Long cartProductId;

    private Long cartId;

    private Long productId;

    private Integer quantity;


    @Builder
    private CartProduct(Long cartProductId, Long cartId, Long productId, Integer quantity) {
        this.cartProductId = cartProductId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

}
