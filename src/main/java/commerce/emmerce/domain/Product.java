package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Product {

    @Id
    @Column(value = "product_id")
    private Long productId;

    private String name;

    private String detail;

    private Integer originalPrice;  // 정가

    private Integer discountPrice;  // 할인가

    private Integer discountRate;  // 할인율

    private Integer stockQuantity;  // 재고 수량

    private Double starScore;   // 별점

    // text[] 자료형으로 저장
    private List<String> titleImgList = new ArrayList<>();   // 대표 이미지 목록

    private List<String> detailImgList = new ArrayList<>();   // 상세 이미지 목록

    private String seller;  // 판매자

    private LocalDateTime enrollTime;  // 등록 시간


    @Builder(builderMethodName = "createProduct")
    private Product(Long productId, String name, String detail, Integer originalPrice, Integer discountPrice, Integer discountRate, Integer stockQuantity,
                    Double starScore, List<String> titleImgList, List<String> detailImgList, String seller, LocalDateTime enrollTime) {
        this.productId = productId;
        this.name = name;
        this.detail = detail;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
        this.starScore = starScore;
        this.titleImgList = titleImgList;
        this.detailImgList = detailImgList;
        this.seller = seller;
        this.enrollTime = enrollTime;
    }


    public void updateStarScore(double starScore) {
        this.starScore = starScore;
    }

    public void updateStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void updateProduct(String name, String detail, int originalPrice, int discountPrice, int discountRate,
                              int stockQuantity, List<String> titleImgList, List<String> detailImgList) {
        this.name = name;
        this.detail = detail;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
        this.titleImgList = titleImgList;
        this.detailImgList = detailImgList;
    }

}
