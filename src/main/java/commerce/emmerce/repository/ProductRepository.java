package commerce.emmerce.repository;

import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.SearchParamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class ProductRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Product product) {
        String insertQuery = """
                insert into product (name, detail, original_price, discount_price, discount_rate, stock_quantity,
                                    star_score, title_img, detail_img_list, brand, enroll_time)
                values (:name, :detail, :originalPrice, :discountPrice, :discountRate, :stockQuantity,
                        :starScore, :titleImg, :detailImgList, :brand, :enrollTime)
                """;

        String updateQuery = """
                update product
                set name = :name, detail = :detail, original_price = :originalPrice,
                    discount_price = :discountPrice, discount_rate = :discountRate, stock_quantity = :stockQuantity,
                    star_score = :starScore, title_img = :titleImg, detail_img_list = :detailImgList,
                    brand = :brand, enroll_time = :enrollTime
                where product_id = :productId
                """;

        String query = product.getProductId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("name", product.getName())
                .bind("detail", product.getDetail())
                .bind("originalPrice", product.getOriginalPrice())
                .bind("discountPrice", product.getDiscountPrice())
                .bind("discountRate", product.getDiscountRate())
                .bind("stockQuantity", product.getStockQuantity())
                .bind("starScore", product.getStarScore())
                .bind("titleImg", product.getTitleImg())
                .bind("detailImgList", product.getDetailImgList().toArray())
                .bind("brand", product.getBrand())
                .bind("enrollTime", product.getEnrollTime());

        if(product.getProductId() != null) {
            executeSpec = executeSpec.bind("productId", product.getProductId());
        }

        return executeSpec.then();
    }

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

    public Mono<ProductDTO.DetailResp> findDetailById(Long productId) {
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
                .map(row -> ProductDTO.DetailResp.builder()
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

    public Flux<ProductDTO.ListResp> findLatestProducts(int size) {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                left join likes l on l.product_id = p.product_id
                group by p.product_id
                order by p.enroll_time desc
                limit :size
                """;

        return databaseClient.sql(query)
                .bind("size", size)
                .fetch().all()
                .map(row -> ProductDTO.ListResp.builder()
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

    public Flux<ProductDTO.ListResp> searchProducts(SearchParamDTO searchParamDTO) {
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
                .bind("keyword", searchParamDTO.getKeyword())
                .bind("brand", searchParamDTO.getBrand())
                .bind("limit", searchParamDTO.getLimit())
                .bind("minPrice", searchParamDTO.getMinPrice())
                .bind("maxPrice", searchParamDTO.getMaxPrice())
                .fetch().all()
                .map(row -> ProductDTO.ListResp.builder()
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

    public Mono<Long> searchProductsCount(SearchParamDTO searchParamDTO) {
        String query = """
                select count(*) as count
                from product p
                where (p.name like :keyword or p.detail like :keyword)
                    and p.brand like :brand
                    and p.discount_price between :minPrice and :maxPrice
                limit :limit
                """;

        return databaseClient.sql(query)
                .bind("keyword", searchParamDTO.getKeyword())
                .bind("brand", searchParamDTO.getBrand())
                .bind("limit", searchParamDTO.getLimit())
                .bind("minPrice", searchParamDTO.getMinPrice())
                .bind("maxPrice", searchParamDTO.getMaxPrice())
                .fetch().one()
                .map(result -> (Long) result.get("count"));
    }

    public Flux<ProductDTO.ListResp> findHotDealProducts(int size) {
        String query = """
                select p.*, count(l.*) as like_count
                from product p
                left join likes l on l.product_id = p.product_id
                group by p.product_id
                order by p.discount_rate desc
                limit :size
                """;

        return databaseClient.sql(query)
                .bind("size", size)
                .fetch().all()
                .map(row -> ProductDTO.ListResp.builder()
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

    public Flux<ProductDTO.ListResp> findRankingProducts(int size) {
        String query = """
                select p.product_id as product_id,
                        p.name as name,
                        p.original_price as original_price,
                        p.discount_price as discount_price,
                        p.discount_rate as discount_rate,
                        p.star_score as star_score,
                        p.title_img as title_img,
                        p.brand as brand,
                        count(l.*) as like_count
                from order_product op
                left join product p on p.product_id = op.product_id
                left join likes l on l.product_id = op.product_id
                group by p.product_id,
                        p.name, 
                        p.original_price, 
                        p.discount_price, 
                        p.discount_rate, 
                        p.star_score, 
                        p.title_img, 
                        p.brand
                order by sum(op.total_count) desc,
                        p.enroll_time asc
                limit :size
                """;

        return databaseClient.sql(query)
                .bind("size", size)
                .fetch().all()
                .map(row -> ProductDTO.ListResp.builder()
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
