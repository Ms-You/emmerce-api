package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

        private Integer discountRate;

        private Integer stockQuantity;

        private Double starScore;

        private List<String> titleImgList = new ArrayList<>();

        private List<String> detailImgList = new ArrayList<>();

        private String seller;
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

        private List<String> titleImgList = new ArrayList<>();

        private List<String> detailImgList = new ArrayList<>();

        private String seller;
    }

}
