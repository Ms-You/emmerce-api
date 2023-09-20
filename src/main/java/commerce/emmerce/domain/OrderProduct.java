package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderProduct {

    @Id
    @GeneratedValue
    @Column(name = "order_product_id")
    private Long id;

    private Integer totalPrice;

    private Integer totalCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    private OrderProduct(Integer totalPrice, Integer totalCount, Order order, Product product) {
        this.totalPrice = totalPrice;
        this.totalCount = totalCount;
        this.order = order;
        this.product = product;

        order.getOrderProductList().add(this);
        product.getOrderProductList().add(this);
    }
}
