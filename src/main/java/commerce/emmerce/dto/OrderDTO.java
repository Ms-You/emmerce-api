package commerce.emmerce.dto;

import commerce.emmerce.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {


    @Getter
    @NoArgsConstructor
    public static class OrderReq {
        // 주문 상품 목록
        private List<OrderProductReq> orderProductList;
        // 배송 정보
        private DeliveryDTO.DeliveryReq deliveryReq;
    }


    @Getter
    @NoArgsConstructor
    public static class OrderProductReq {
        private Long productId;
        private Integer totalCount;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderResp {
        private Long orderId;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private List<OrderProductResp> orderProductRespList;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderProductResp {
        private Long productId;
        private String name;
        private String titleImg;
        private String brand;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer quantity;
    }


}
