package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryProductDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResp {
        private Long productId;
        private String name;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer discountRate;
        private Double starScore;
        private String titleImg;
        private Long likeCount;
        private String brand;
    }
}
