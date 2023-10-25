package commerce.emmerce.dto;

import commerce.emmerce.domain.DeliveryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DeliveryDTO {

    @Getter
    @NoArgsConstructor
    public static class DeliveryReq {
        // 주문자 정보
        private String name;
        private String tel;
        private String email;
        // 배송지 정보
        private String city;
        private String street;
        private String zipcode;
    }


    @Getter
    @NoArgsConstructor
    public static class StatusReq {
        private DeliveryStatus deliveryStatus;
    }


}
