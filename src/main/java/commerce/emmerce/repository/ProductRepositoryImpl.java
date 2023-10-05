package commerce.emmerce.repository;

import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl {

    private final DatabaseClient databaseClient;


    public Mono<Void> save(Product product) {
        String query = """
                insert into product (name, detail, original_price, discount_price, discount_rate, stock_quantity, star_score, title_img_list, detail_img_list, seller)
                    values(:name, :detail, :originalPrice, :discountPrice, :discountRate, :stockQuantity, :starScore, :titleImgList, :detailImgList, :seller)
                """;
        return databaseClient.sql(query)
                .bind("name", product.getName())
                .bind("detail", product.getDetail())
                .bind("originalPrice", product.getOriginalPrice())
                .bind("discountPrice", product.getDiscountPrice())
                .bind("discountRate", product.getDiscountRate())
                .bind("stockQuantity", product.getStockQuantity())
                .bind("starScore", product.getStarScore())
                .bind("titleImgList", product.getTitleImgList().toArray())
                .bind("detailImgList", product.getDetailImgList().toArray())
                .bind("seller", product.getSeller())
                .then();
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
                        .titleImgList(Arrays.asList((String[]) row.get("title_img_list")))
                        .detailImgList(Arrays.asList((String[]) row.get("detail_img_list")))
                        .seller((String) row.get("seller"))
                        .build());
    }



}
