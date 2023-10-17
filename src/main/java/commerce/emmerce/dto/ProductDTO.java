package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    public static class ProductUpdateReq {
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
    public static class ProductDetailResp {
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
        private List<ReviewDTO.ReviewResp> reviewRespList;

        public void setReviewRespList(List<ReviewDTO.ReviewResp> reviewRespList) {
            this.reviewRespList = reviewRespList;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductListResp {
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
