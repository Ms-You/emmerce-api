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
public class OrderProduct {

    @Id
    @Column(name = "order_product_id")
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
