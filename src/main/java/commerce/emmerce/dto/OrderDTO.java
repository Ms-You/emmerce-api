package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class OrderDTO {


    @Getter
    @NoArgsConstructor
    public static class OrderReq {
        // 주문 상품 목록
        private List<OrderProductReq> orderProductList;
        // 배송 정보
        private DeliveryDTO.DeliveryReq deliveryReq;
        // 결제 정보
        private PaymentDTO.PaymentReq paymentReq;
    }


    @Getter
    @NoArgsConstructor
    public static class OrderProductReq {
        private Long productId;
        private Integer totalCount;
        private Integer totalPrice;
    }



}
