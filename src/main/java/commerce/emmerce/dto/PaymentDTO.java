package commerce.emmerce.dto;

import commerce.emmerce.domain.PaymentMethod;
import commerce.emmerce.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class PaymentDTO {

    @Getter
    @NoArgsConstructor
    public static class PaymentReq {
        private BigDecimal amount;
        private PaymentStatus paymentStatus;
        private PaymentMethod paymentMethod;
        private Long orderId;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResp {
        private Long paymentId;
        private BigDecimal amount;
        private PaymentStatus paymentStatus;
        private PaymentMethod paymentMethod;
    }


}
