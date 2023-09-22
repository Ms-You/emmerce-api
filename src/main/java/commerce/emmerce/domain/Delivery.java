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
public class Delivery {

    @Id
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;    // 주소

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;  // 배송 상태

//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "delivery")
    private Order order;


    @Builder(builderMethodName = "createDelivery")
    private Delivery(Address address, DeliveryStatus deliveryStatus, Order order) {
        this.address = address;
        this.deliveryStatus = deliveryStatus;
        this.order = order;
    }

}
