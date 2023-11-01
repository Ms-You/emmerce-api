package commerce.emmerce.repository;

import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.dto.CategoryProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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


    public Flux<CategoryProductDTO.ListResp> findAllByCategoryId(Long categoryId, String keyword, String brand, int limit, int minPrice, int maxPrice) {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                inner join category_product cp on cp.product_id = p.product_id
                left join likes l on l.product_id = p.product_id
                where cp.category_id = :categoryId
                    and (p.name like :keyword or p.detail like :keyword)
                    and p.brand like :brand
                    and p.discount_price between :minPrice and :maxPrice
                group by p.product_id
                limit :limit
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryId)
                .bind("keyword", '%' + keyword + '%')
                .bind("brand", '%' + brand + '%')
                .bind("limit", limit)
                .bind("minPrice", minPrice)
                .bind("maxPrice", maxPrice)
                .fetch().all()
                .map(row -> CategoryProductDTO.ListResp.builder()
                        .productId((Long) row.get("product_id"))
                        .name((String) row.get("name"))
                        .originalPrice((Integer) row.get("original_price"))
                        .discountPrice((Integer) row.get("discount_price"))
                        .discountRate((Integer) row.get("discount_rate"))
                        .starScore((Double) row.get("star_score"))
                        .titleImg((String) row.get("title_img"))
                        .likeCount((Long) row.get("like_count"))
                        .brand((String) row.get("brand"))
                        .build());
    }


    public Mono<Long> findCountByCategoryId(Long categoryId, String keyword, String brand, int limit, int minPrice, int maxPrice) {
        String query = """
                select count(*) as count
                from product p
                inner join category_product cp on cp.product_id = p.product_id
                where cp.category_id = :categoryId
                    and (p.name like :keyword or p.detail like :keyword)
                    and p.brand like :brand
                    and p.discount_price between :minPrice and :maxPrice
                limit :limit
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryId)
                .bind("keyword", '%' + keyword + '%')
                .bind("brand", '%' + brand + '%')
                .bind("limit", limit)
                .bind("minPrice", minPrice)
                .bind("maxPrice", maxPrice)
                .fetch().one()
                .map(result -> (Long) result.get("count"));
    }


    public Flux<CategoryProduct> findByProductId(Long productId) {
        String query = """
                select * 
                from category_product cp
                where cp.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().all()
                .map(row -> CategoryProduct.builder()
                        .categoryProductId((Long) row.get("category_product_id"))
                        .categoryId((Long) row.get("category_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }

}
