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
    private Long deliveryId;

    private String city;

    private String street;

    private String zipcode;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;  // 배송 상태

    private Long orderId;


    @Builder(builderMethodName = "createDelivery")
    private Delivery(String city, String street, String zipcode, DeliveryStatus deliveryStatus, Long orderId) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.deliveryStatus = deliveryStatus;
        this.orderId = orderId;
    }

}
