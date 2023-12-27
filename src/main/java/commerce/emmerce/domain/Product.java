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

    private Double starScore;   // 평균 별점

    private Integer totalReviews;   // 리뷰 개수

    private String titleImg;

    private List<String> detailImgList = new ArrayList<>();   // 상세 이미지 목록

    private String brand;

    private LocalDateTime enrollTime;


    @Builder(builderMethodName = "createProduct")
    private Product(Long productId, String name, String detail, Integer originalPrice, Integer discountPrice, Integer discountRate, Integer stockQuantity,
                    Double starScore, Integer totalReviews, String titleImg, List<String> detailImgList, String brand, LocalDateTime enrollTime) {
        this.productId = productId;
        this.name = name;
        this.detail = detail;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
        this.starScore = starScore;
        this.totalReviews = totalReviews;
        this.titleImg = titleImg;
        this.detailImgList = detailImgList;
        this.brand = brand;
        this.enrollTime = enrollTime;
    }


    public void updateStarScore(double starScore) {
        this.starScore = starScore;
    }

    public void updateTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public void updateStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void updateProduct(String name, String detail, int originalPrice, int discountPrice, int discountRate,
                              int stockQuantity, String titleImg, List<String> detailImgList) {
        this.name = name;
        this.detail = detail;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
        this.titleImg = titleImg;
        this.detailImgList = detailImgList;
    }

}
