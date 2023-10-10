package commerce.emmerce.repository;

import commerce.emmerce.domain.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class OrderProductRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(OrderProduct orderProduct) {
        String query = """
                insert into order_product (total_price, order_id, product_id) values (:totalPrice, :orderId, :productId)
                """;

        return databaseClient.sql(query)
                .bind("totalPrice", orderProduct.getTotalPrice())
                .bind("orderId", orderProduct.getOrderId())
                .bind("productId", orderProduct.getProductId())
                .then();
    }



}
