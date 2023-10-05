package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table
public class OrderProduct {

    @Id
    @Column(value = "order_product_id")
    private Long orderProductId;

    private Integer totalPrice;

    private Integer totalCount;

    private Long orderId;

    private Long productId;


    @Builder
    private OrderProduct(Integer totalPrice, Integer totalCount, Long orderId, Long productId) {
        this.totalPrice = totalPrice;
        this.totalCount = totalCount;
        this.orderId = orderId;
        this.productId = productId;
    }

}
