package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class CategoryProduct {

    @Id
    @Column(name = "category_product_id")
    private Long id;

    private Long categoryId;

    private Long productId;


    @Builder
    private CategoryProduct(Long categoryId, Long productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }

}
