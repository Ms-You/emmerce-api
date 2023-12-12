package commerce.emmerce.dto;

import commerce.emmerce.domain.*;
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
    public static class OrderCreateResp {
        private Long orderId;
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

        public static OrderResp transfer(Order order, List<OrderProductResp> orderProductRespList) {
            return OrderResp.builder()
                    .orderId(order.getOrderId())
                    .orderDate(order.getOrderDate())
                    .orderStatus(order.getOrderStatus())
                    .orderProductRespList(orderProductRespList)
                    .build();
        }
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
        private boolean reviewStatus;
        private DeliveryStatus deliveryStatus;

        public static OrderProductResp transfer(Product product, OrderProduct orderProduct, Boolean reviewStatus, DeliveryStatus deliveryStatus) {
            return OrderProductResp.builder()
                    .productId(product.getProductId())
                    .name(product.getName())
                    .titleImg(product.getTitleImg())
                    .brand(product.getBrand())
                    .originalPrice(product.getOriginalPrice())
                    .discountPrice(product.getDiscountPrice())
                    .quantity(orderProduct.getTotalCount())
                    .reviewStatus(reviewStatus)
                    .deliveryStatus(deliveryStatus)
                    .build();
        }
    }


}
