package commerce.emmerce.repository;

import commerce.emmerce.domain.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
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


    public Flux<OrderProduct> findByOrderId(Long orderId) {
        String query = """
                select *
                from order_product op
                where op.order_id = :orderId
                """;

        return databaseClient.sql(query)
                .bind("orderId", orderId)
                .fetch().all()
                .map(row -> OrderProduct.builder()
                        .orderProductId((Long) row.get("order_product_id"))
                        .totalPrice((Integer) row.get("total_price"))
                        .totalCount((Integer) row.get("total_count"))
                        .orderId((Long) row.get("order_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }



}
