package commerce.emmerce.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDTO {

    @Getter
    @NoArgsConstructor
    public static class ProductReq {
        private String name;
        private String detail;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer stockQuantity;
        private String brand;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateReq {
        private String name;
        private String detail;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer stockQuantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResp {
        private Long productId;
        private String name;
        private String detail;
        private Integer originalPrice;
        private Integer discountPrice;
        private Integer discountRate;
        private Integer stockQuantity;
        private Double starScore;
        private String titleImg;
        private List<String> detailImgList;
        private String brand;
        private LocalDateTime enrollTime;
        private Long likeCount;
    }

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
