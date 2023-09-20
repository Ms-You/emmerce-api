package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @CreatedDate
    private LocalDateTime orderDate;   // 주문 날짜

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;  // 주문 상태


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Builder
    private Order(LocalDateTime orderDate, OrderStatus orderStatus, Member member,  Delivery delivery) {
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.member = member;
        this.delivery = delivery;
    }


}
