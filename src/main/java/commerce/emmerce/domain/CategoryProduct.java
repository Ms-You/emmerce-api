package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CategoryProduct {

    @Id
    @GeneratedValue
    @Column(name = "category_product_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    @Builder
    private CategoryProduct(Category category, Product product) {
        this.category = category;
        this.product = product;

        category.getCategoryProductList().add(this);
        product.getCategoryProductList().add(this);
    }

}
