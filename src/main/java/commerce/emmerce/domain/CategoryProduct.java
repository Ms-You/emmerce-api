package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class CategoryProduct {

    @Id
    @Column(value = "category_product_id")
    private Long categoryProductId;

    private Long categoryId;

    private Long productId;


    @Builder
    private CategoryProduct(Long categoryProductId, Long categoryId, Long productId) {
        this.categoryProductId = categoryProductId;
        this.categoryId = categoryId;
        this.productId = productId;
    }

}
