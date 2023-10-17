package commerce.emmerce.dto;

import commerce.emmerce.domain.PaymentMethod;
import commerce.emmerce.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentDTO {

    @Getter
    @NoArgsConstructor
    public static class PaymentReq {
        private PaymentMethod paymentMethod;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResp {
        private Long paymentId;
        private Integer amount;
        private PaymentStatus paymentStatus;
        private PaymentMethod paymentMethod;
    }


}
