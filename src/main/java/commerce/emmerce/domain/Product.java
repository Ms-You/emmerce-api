package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private Integer original_price;  // 정가

    private Integer discount_price;  // 할인가

    private Integer discount_rate;  // 할인율

    private Integer stock_quantity;  // 재고 수량

    private List<String> title_img_list = new ArrayList<>();   // 대표 이미지 목록

    private List<String> detail_img_list = new ArrayList<>();   // 상세 이미지 목록

    private String seller;  // 판매자


    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<CartProduct> cart_product_list = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<OrderProduct> order_product_list = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<Heart> heart_list = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<Review> review_list = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<CategoryProduct> category_product_list = new ArrayList<>();

}
