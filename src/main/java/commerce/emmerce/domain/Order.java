package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table("orders")
public class Order {

    @Id
    @Column(value = "order_id")
    private Long orderId;

    @CreatedDate
    private LocalDateTime orderDate;   // 주문 날짜

    private OrderStatus orderStatus;  // 주문 상태

    private Long memberId;


    @Builder(builderMethodName = "createOrder")
    private Order(LocalDateTime orderDate, OrderStatus orderStatus, Long memberId) {
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.memberId = memberId;
    }

}
