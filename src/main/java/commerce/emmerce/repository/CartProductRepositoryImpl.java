package commerce.emmerce.repository;

import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.CartProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class CartProductRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(CartProduct cartProduct) {
        String query = """
                insert into cart_product (cart_id, product_id, quantity) values (:cartId, :productId, :quantity)
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartProduct.getCartId())
                .bind("productId", cartProduct.getProductId())
                .bind("quantity", cartProduct.getQuantity())
                .then();
    }


    public Mono<CartProduct> findByCartIdAndProductId(Long cartId, Long productId) {
        String query = """
                select * 
                from cart_product cp
                where cp.cart_id = :cartId and cp.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .bind("productId", productId)
                .fetch()
                .one()
                .map(row -> CartProduct.builder()
                        .cartProductId((Long) row.get("cart_product_id"))
                        .cartId((Long) row.get("cart_id"))
                        .productId((Long) row.get("product_id"))
                        .quantity((Integer) row.get("quantity"))
                        .build());
    }


    public Flux<Product> findAllProductsByCartId(Long cartId) {
        String query = """
                select * 
                from product p
                inner join cart_product cp on cp.product_id = p.product_id
                where cp.cart_id = :cartId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .fetch()
                .all()
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


    public Mono<Void> delete(CartProduct cartProduct) {
        String query = """
                delete
                from cart_product cp
                where cp.cart_id = :cartId and cp.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartProduct.getCartId())
                .bind("productId", cartProduct.getProductId())
                .then();
    }



    public Flux<CartProductDTO.CartProductListResp> findAllByCartId(Long cartId) {
        String query = """
                select p.*, cp.quantity as quantity
                from product p
                inner join cart_product cp on p.product_id = cp.product_id
                where cp.cart_id = :cartId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .map(row -> CartProductDTO.CartProductListResp.builder()
                        .productId((Long) row.get("product_id"))
                        .name((String) row.get("name"))
                        .titleImgList(Arrays.asList((String[]) row.get("title_img_list")))
                        .discountPrice((Integer) row.get("discount_price"))
                        .totalCount((Integer) row.get("quantity"))
                        .totalPrice((Integer) row.get("discount_price") * (Integer) row.get("quantity"))
                        .build())
                .all();
    }

}
