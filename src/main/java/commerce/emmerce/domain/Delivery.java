package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;    // 주소

    @Enumerated(EnumType.STRING)
    private DeliveryStatus delivery_status;  // 배송 상태


    @OneToOne(fetch = FetchType.LAZY, mappedBy = "delivery")
    private Order order;

}
