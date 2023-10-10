package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CartProductDTO {

    @Getter
    @NoArgsConstructor
    public static class CartProductReq {
        private Long productId;
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartProductListResp {
        private Long productId;
        private String name;
        private List<String> titleImgList;
        private Integer discountPrice;
        private Integer totalCount;
        private Integer totalPrice;
    }


}
