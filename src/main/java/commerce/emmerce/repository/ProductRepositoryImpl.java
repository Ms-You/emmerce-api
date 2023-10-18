package commerce.emmerce.repository;

import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl {

    private final DatabaseClient databaseClient;


    public Mono<Product> findById(Long productId) {
        String query = """
                select * 
                from product p 
                where p.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().one()
                .map(row -> Product.createProduct()
                        .productId((Long) row.get("product_id"))
                        .name((String) row.get("name"))
                        .detail((String) row.get("detail"))
                        .originalPrice((Integer) row.get("original_price"))
                        .discountPrice((Integer) row.get("discount_price"))
                        .discountRate((Integer) row.get("discount_rate"))
                        .stockQuantity((Integer) row.get("stock_quantity"))
                        .starScore((Double) row.get("star_score"))
                        .titleImg((String) row.get("title_img"))
                        .detailImgList(Arrays.asList((String[]) row.get("detail_img_list")))
                        .brand((String) row.get("brand"))
                        .enrollTime((LocalDateTime) row.get("enroll_time"))
                        .build());
    }

    public Mono<ProductDTO.ProductDetailResp> findDetailById(Long productId) {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                left join likes l on p.product_id = l.product_id
                where p.product_id = :productId
                group by p.product_id
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().one()
                .map(row -> ProductDTO.ProductDetailResp.builder()
                        .productId((Long) row.get("product_id"))
                        .name((String) row.get("name"))
                        .detail((String) row.get("detail"))
                        .originalPrice((Integer) row.get("original_price"))
                        .discountPrice((Integer) row.get("discount_price"))
                        .discountRate((Integer) row.get("discount_rate"))
                        .stockQuantity((Integer) row.get("stock_quantity"))
                        .starScore((Double) row.get("star_score"))
                        .titleImg((String) row.get("title_img"))
                        .detailImgList(Arrays.asList((String[]) row.get("detail_img_list")))
                        .brand((String) row.get("brand"))
                        .enrollTime((LocalDateTime) row.get("enroll_time"))
                        .likeCount((Long) row.get("like_count"))
                        .build());
    }


    public Flux<Product> findAll() {
        String query = """
                select *
                from product p
                """;

        return databaseClient.sql(query)
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
                        .titleImg((String) row.get("title_img"))
                        .detailImgList(Arrays.asList((String[]) row.get("detail_img_list")))
                        .brand((String) row.get("brand"))
                        .enrollTime((LocalDateTime) row.get("enroll_time"))
                        .build());
    }


    public Flux<ProductDTO.ProductListResp> findLatestProducts() {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                left join likes l on l.product_id = p.product_id
                group by p.product_id
                order by p.enroll_time desc
                limit 12
                """;

        return databaseClient.sql(query)
                .fetch().all()
                .map(row -> ProductDTO.ProductListResp.builder()
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


    public Flux<ProductDTO.ProductListResp> searchProducts(String keyword, String brand, int limit, int minPrice, int maxPrice) {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                left join likes l on l.product_id = p.product_id
                where (p.name like :keyword or p.detail like :keyword)
                    and p.brand like :brand
                    and p.discount_price between :minPrice and :maxPrice
                group by p.product_id
                limit :limit
                """;

        return databaseClient.sql(query)
                .bind("keyword", '%' + keyword + '%')
                .bind("brand", '%' + brand + '%')
                .bind("limit", limit)
                .bind("minPrice", minPrice)
                .bind("maxPrice", maxPrice)
                .fetch().all()
                .map(row -> ProductDTO.ProductListResp.builder()
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


}
