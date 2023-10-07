package commerce.emmerce.repository;

import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class CategoryProductRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(CategoryProduct categoryProduct) {
        String query = """
                insert into category_product (category_id, product_id) values(:categoryId, :productId)
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryProduct.getCategoryId())
                .bind("productId", categoryProduct.getProductId())
                .then();
    }

    public Mono<Void> deleteByCategoryIdAndProductId(Long categoryId, Long productId) {
        String query = """
                delete from category_product where category_id = :categoryId and product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryId)
                .bind("productId", productId)
                .then();
    }

    public Flux<Product> productListByCategory(Long categoryId) {
        String query = """
                select *
                from product p
                inner join category_product cp on cp.product_id = p.product_id
                where cp.category_id = :categoryId
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryId)
                .fetch().all()
                .map(row -> Product.createProduct()
                            .productId((Long) row.get("product_id"))
                            .name((String) row.get("name"))
                            .detail((String) row.get("detail"))
                            .originalPrice((Integer) row.get("original_price"))
                            .discountPrice((Integer) row.get("discount_price"))
                            .discountRate((Integer) row.get("discount_rate"))
                            .stockQuantity((Integer) row.get("stock_quantity"))
                            .starScore((Double) row.get("star_score"))
                            .titleImgList(Arrays.asList((String[]) row.get("title_img_list")))
                            .detailImgList(Arrays.asList((String[]) row.get("detail_img_list")))
                            .seller((String) row.get("seller"))
                            .build());
    }

}
