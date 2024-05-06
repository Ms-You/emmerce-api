package commerce.emmerce.repository;

import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.dto.CartProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CartProductRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(CartProduct cartProduct) {
        String insertQuery = """
                insert into cart_product (cart_id, product_id, quantity) 
                values (:cartId, :productId, :quantity)
                """;

        String updateQuery = """
                update cart_product
                set cart_id = :cartId, product_id = :productId, quantity = :quantity
                where cart_product_id = :cartProductId
                """;

        String query = cartProduct.getCartProductId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("cartId", cartProduct.getCartId())
                .bind("productId", cartProduct.getProductId())
                .bind("quantity", cartProduct.getQuantity());

        if(cartProduct.getCartProductId() != null) {
            executeSpec = executeSpec.bind("cartProductId", cartProduct.getCartProductId());
        }

        return executeSpec
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<CartProduct> findByCartIdAndCartProductId(Long cartId, Long cartProductId) {
        String query = """
                select *
                from cart_product cp
                where cp.cart_id = :cartId
                    and cp.cart_product_id = :cartProductId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .bind("cartProductId", cartProductId)
                .fetch().one()
                .map(row -> CartProduct.builder()
                        .cartProductId((Long) row.get("cart_product_id"))
                        .cartId((Long) row.get("cart_id"))
                        .productId((Long) row.get("product_id"))
                        .quantity((Integer) row.get("quantity"))
                        .build());
    }

    public Mono<CartProduct> findByCartIdAndProductId(Long cartId, Long productId) {
        String query = """
                select * 
                from cart_product cp
                where cp.cart_id = :cartId 
                    and cp.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .bind("productId", productId)
                .fetch().one()
                .map(row -> CartProduct.builder()
                        .cartProductId((Long) row.get("cart_product_id"))
                        .cartId((Long) row.get("cart_id"))
                        .productId((Long) row.get("product_id"))
                        .quantity((Integer) row.get("quantity"))
                        .build());
    }

    public Mono<Void> delete(CartProduct cartProduct) {
        String query = """
                delete
                from cart_product cp
                where cp.cart_product_id = :cartProductId
                """;

        return databaseClient.sql(query)
                .bind("cartProductId", cartProduct.getCartProductId())
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<Long> deleteAll(Long cartId) {
        String query = """
                delete
                from cart_product cp
                where cp.cart_id = :cartId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .fetch()
                .rowsUpdated();
    }

    public Flux<CartProductDTO.ListResp> findAllByCartId(Long cartId) {
        String query = """
                select p.*, cp.cart_product_id as cart_product_id, cp.quantity as quantity
                from product p
                inner join cart_product cp on p.product_id = cp.product_id
                where cp.cart_id = :cartId
                """;

        return databaseClient.sql(query)
                .bind("cartId", cartId)
                .fetch().all()
                .map(row -> CartProductDTO.ListResp.builder()
                        .cartProductId((Long) row.get("cart_product_id"))
                        .productId((Long) row.get("product_id"))
                        .name((String) row.get("name"))
                        .titleImg((String) row.get("title_img"))
                        .originalPrice((Integer) row.get("original_price"))
                        .discountPrice((Integer) row.get("discount_price"))
                        .quantity((Integer) row.get("quantity"))
                        .totalPrice((Integer) row.get("discount_price") * (Integer) row.get("quantity"))
                        .brand((String) row.get("brand"))
                        .build());
    }

}
