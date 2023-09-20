package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CartProduct {

    @Id
    @GeneratedValue
    @Column(name = "cart_product_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    @Builder
    private CartProduct(Cart cart, Product product) {
        this.cart = cart;
        this.product = product;

        cart.getCartProductList().add(this);
        product.getCartProductList().add(this);
    }

}
