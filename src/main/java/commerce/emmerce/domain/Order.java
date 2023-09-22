package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("orders")
public class Order {

    @Id
    @Column(name = "order_id")
    private Long id;

    @CreatedDate
    private LocalDateTime orderDate;   // 주문 날짜

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;  // 주문 상태

    private Long memberId;


    @Builder(builderMethodName = "createOrder")
    private Order(LocalDateTime orderDate, OrderStatus orderStatus, Long memberId) {
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.memberId = memberId;
    }

}
