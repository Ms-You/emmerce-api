package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CartProductDTO {

    @Getter
    @NoArgsConstructor
    public static class EnrollReq {
        private Long productId;
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResp {
        private Long cartProductId;
        private Long productId;
        private String name;
        private String titleImg;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer quantity;
        private Integer totalPrice;
        private String brand;
    }


}
