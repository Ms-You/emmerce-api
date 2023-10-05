package commerce.emmerce.repository;

import commerce.emmerce.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Product product){
        String query = """
                INSERT INTO product (name, detail, original_price,
                discount_price, discount_rate, stock_quantity,
                star_score, title_img_list, detail_img_list, seller) VALUES (:name, :detail, :originalPrice,
                :discountPrice, :discountRate, :stockQuantity, :starScore, :titleImgList, :detailImgList, :seller);
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
}
