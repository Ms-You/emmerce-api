package commerce.emmerce.dto;

import commerce.emmerce.domain.DeliveryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DeliveryDTO {

    @Getter
    @NoArgsConstructor
    public static class DeliveryReq {
        private String city;
        private String street;
        private String zipcode;
        private Long orderId;
    }


    @Getter
    @NoArgsConstructor
    public static class DeliveryStatusReq {
        private DeliveryStatus deliveryStatus;
    }


}
