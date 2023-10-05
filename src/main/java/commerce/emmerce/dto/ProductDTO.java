package commerce.emmerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class ProductDTO {
    @Getter
    @NoArgsConstructor
    public static class ProductReq{
        private String name;
        private String detail;
        private Integer originalPrice;  // 정가
        private Integer discountPrice;  // 할인가
        private Integer discountRate;  // 할인율
        private Integer stockQuantity;  // 재고 수량
        private Double starScore;   // 별점
        // jsonb 형태로 저장 (JSON으로 변환하여 저장)
        private List<String> titleImgList = new ArrayList<>();   // 대표 이미지 목록
        private List<String> detailImgList = new ArrayList<>();   // 상세 이미지 목록
        private String seller;  // 판매자
    }

    @Getter
    @NoArgsConstructor
    @Builder
    public static class ProductResp{

    }
}
