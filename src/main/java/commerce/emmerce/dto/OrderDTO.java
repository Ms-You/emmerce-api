package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderDTO {

    @Getter
    @NoArgsConstructor
    public static class OrderCartProductReq {
        private Long productId;
        private Integer totalCount;
        private Integer totalPrice;
    }



}
