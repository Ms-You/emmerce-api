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
        private String titleImg;
        private List<String> detailImgList = new ArrayList<>();
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
        private String titleImg;
        private List<String> detailImgList = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResp {
        // 상위 카테고리 정보
        @Setter
        private List<CategoryDTO.InfoResp> categoryInfoRespList;

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
        // 리뷰 목록
        @Setter
        private List<ReviewDTO.ReviewResp> reviewRespList;
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
