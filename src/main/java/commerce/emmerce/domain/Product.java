package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String detail;

    private Integer originalPrice;  // 정가

    private Integer discountPrice;  // 할인가

    private Integer discountRate;  // 할인율

    private Integer stockQuantity;  // 재고 수량

    private List<String> titleImgList = new ArrayList<>();   // 대표 이미지 목록

    private List<String> detailImgList = new ArrayList<>();   // 상세 이미지 목록

    private String seller;  // 판매자


    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<CartProduct> cartProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<Heart> heartList = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<CategoryProduct> categoryProductList = new ArrayList<>();


    @Builder(builderMethodName = "createProduct")
    private Product(String name, String detail, Integer originalPrice, Integer discountPrice, Integer discountRate,
                    Integer stockQuantity, List<String> titleImgList, List<String> detailImgList, String seller) {
        this.name = name;
        this.detail = detail;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
        this.titleImgList = titleImgList;
        this.detailImgList = detailImgList;
        this.seller = seller;
    }

}
