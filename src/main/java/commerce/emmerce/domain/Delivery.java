package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Delivery {

    @Id
    @Column(value = "delivery_id")
    private Long deliveryId;

    private String name;

    private String tel;

    private String email;

    private String city;

    private String street;

    private String zipcode;

    private DeliveryStatus deliveryStatus;  // 배송 상태

    private Long orderId;

    private Long productId;


    @Builder(builderMethodName = "createDelivery")
    private Delivery(Long deliveryId, String name, String tel, String email, String city, String street,
                     String zipcode, DeliveryStatus deliveryStatus, Long orderId, Long productId) {
        this.deliveryId = deliveryId;
        this.name = name;
        this.tel = tel;
        this.email = email;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.deliveryStatus = deliveryStatus;
        this.orderId = orderId;
        this.productId = productId;
    }

    public void updateStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

}
